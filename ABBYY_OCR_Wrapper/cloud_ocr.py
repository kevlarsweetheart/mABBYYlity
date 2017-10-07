from lxml import etree
import requests
import time

DEFAULT_EXPORT_FORMAT='txt'

class CloudOCR:
    abbyy_server = 'http://cloud.ocrsdk.com'


    def __init__(self, app_id, app_pass):
        self.session = requests.Session()
        self.auth = (app_id, app_pass)


    def get_file_by_name(self, file_name):
        infile = open(file_name, 'rb')
        file_to_post = {infile.name: infile}
        return file_to_post


    def handle_reply(self, reply):
        reply.raise_for_status()
        xml_reply = etree.fromstring(reply.content)

        if xml_reply.xpath('//error/message'):
            raise Exception(xml_reply.xpath('//error/message')[0].text)
        elements = xml_reply.xpath('//response')
        if elements.__len__() != 1:
            raise Exception("Bad server response:" + reply)

        return elements


    def submitImage(self, file, **parameters):
        method = '/submitImage'
        reply = self.session.post(self.abbyy_server + method, auth=self.auth, params=parameters, files=file)

        elements = self.handle_reply(reply)
        response = []
        for element in elements[0]:
            response.append(dict(list(zip(list(element.keys()), list(element.values())))))

        return response


    def uploadImages(self, filenames_list):
        collected_responses = []

        first_file = self.get_file_by_name(filenames_list[0])
        params = {}
        response = self.submitImage(first_file, **params)
        collected_responses.append(response[0])
        task_id = response[0].get('id')

        images_cnt = len(filenames_list)
        if images_cnt > 1:
            params['taskId'] = task_id

        for i in range(1, images_cnt):
            curr_file = self.get_file_by_name(filenames_list[i])
            collected_responses.append(self.submitImage(curr_file, **params)[0])

        return collected_responses


    def processDocument(self, **parameters):
        method = '/processDocument'

        reply = self.session.get(self.abbyy_server + method, auth=self.auth, params=parameters)
        elements = self.handle_reply(reply)
        response = []
        for element in elements[0]:
            response.append(dict(list(zip(list(element.keys()), list(element.values())))))

        return response


    def getTaskStatus(self, task_id):
        method = '/getTaskStatus'
        params = {'taskId': task_id}

        reply = self.session.get(self.abbyy_server + method, auth=self.auth, params=params)
        elements = self.handle_reply(reply)
        response = []
        for element in elements[0]:
            response.append(dict(list(zip(list(element.keys()), list(element.values())))))

        return response[0]

    def wait_for_task(self, task, delay_between_status_check=1, timeout=300):
        taskId = task.get('id')
        for i in range(timeout):
            task = self.getTaskStatus(task_id=taskId)
            if task.get('status') == 'InProgress' or task.get('status') == 'Queued':
                delay_between_status_check = int(task['estimatedProcessingTime'])
                time.sleep(delay_between_status_check)
            elif task.get('status') == 'NotEnoughCredits':
                raise Exception('NotEnoughCredits')
            else:
                return task
            raise Exception("OCR Timed out")


    def get_result_url(self, task):
        result = self.wait_for_task(task=task, delay_between_status_check=1, timeout=300)
        return result.get('resultUrl')