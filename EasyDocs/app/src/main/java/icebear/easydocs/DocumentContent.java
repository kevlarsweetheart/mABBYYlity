package icebear.easydocs;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class DocumentContent {
    private String fileName;
    private String fileType;
    private ArrayList<String> paths;
    String updateDateTime;
    SimpleDateFormat format= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault());

    public DocumentContent(String name, String type, String[] paths){
        this.fileName = name;
        this.fileType = type;
        this.paths = new ArrayList<>(Arrays.asList(paths));
        updateDateTime = format.format(new Date());
        return;
    }

    public DocumentContent(String name, String type, String path){
        this.fileName = name;
        this.fileType = type;
        this.paths = new ArrayList<>();
        paths.add(path);
        updateDateTime = format.format(new Date());
        return;
    }

    public String getFileName(){
        return this.fileName;
    }

    public String getFileType(){
        return this.fileType;
    }

    public ArrayList<String> getPathToFile(){
        return this.paths;
    }

    public void addPhoto(String path){
        this.paths.add(path);
        updateDateTime = format.format(new Date());
    }

    public void rename(String name) {
        this.fileName = name;
    }

    public String getUpdateDateTime(){
        return updateDateTime;
    }

    public int getImgsCount(){
        return paths.size();
    }
}
