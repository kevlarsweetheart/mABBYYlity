package icebear.easydocs;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.text.InputType;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.ContextMenu;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath;
    private static final String TAG = "Debugging";
    String pathToPhoto;
    ArrayList<DocumentContent> savedDocs;
    ArrayAdapter adapter;
    private int currListId;
    private String mText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        savedDocs = new ArrayList<DocumentContent>();

        DocumentContent doc = new DocumentContent("Doc", "Kek", "Cheburek");
        savedDocs.add(doc);

        adapter = new DocsListAdapter(this, savedDocs);
        ListView docsView = (ListView) findViewById(R.id.docs_view_id);
        docsView.setAdapter(adapter);

        registerForContextMenu(docsView);

        //Кнопка "Add new document"
        docsView.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        DocumentContent li = savedDocs.get(i);
                        Toast.makeText(MainActivity.this, li.getPathToFile().get(0), Toast.LENGTH_LONG).show();

                    }
                }
        );


        Button take_photo = (Button) findViewById(R.id.button);

        take_photo.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        currListId = -1;
                        Log.i(TAG, "Launching camera");
                        launchCamera();
                    }
                }
        );
    }


    //Block for Taking Image------------------------------------------------------------------------
    public void launchCamera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                pathToPhoto = photoFile.getAbsolutePath();
                Log.i(TAG, pathToPhoto);
            } catch (IOException ex) {
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        File imgFile = new  File(pathToPhoto);

        if(imgFile.exists()){
            if (currListId == -1){
                currListId = savedDocs.size();
                DocumentContent doc = new DocumentContent("Document " + String.valueOf(currListId), "jpg", pathToPhoto);
                savedDocs.add(doc);
            }
            else{
                savedDocs.get(currListId).addPhoto(pathToPhoto);
            }
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getApplicationContext(), "Failed to add photo", Toast.LENGTH_LONG).show();
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    //----------------------------------------------------------------------------------------------


    //Block for context menu------------------------------------------------------------------------
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        currListId = info.position;
        Log.i(TAG, String.valueOf(currListId));
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.docs_menu, menu);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().toString().equals("Add photo")) {
            Toast.makeText(getApplicationContext(), "Add photo", Toast.LENGTH_LONG).show();
            launchCamera();
        }

        else if(item.getTitle().toString().equals("Delete")) {
            Toast.makeText(getApplicationContext(), "Delete", Toast.LENGTH_LONG).show();
            deleteDoc(currListId);
        }
        else if(item.getTitle().toString().equals("Process")) {
            Toast.makeText(getApplicationContext(), "Process", Toast.LENGTH_LONG).show();

        }
        else if(item.getTitle().toString().equals("Rename")) {
            Toast.makeText(getApplicationContext(), "Rename", Toast.LENGTH_LONG).show();
            renameDoc(currListId);
        }
        else if(item.getTitle().toString().equals("..."))
            Toast.makeText(getApplicationContext(),"DO KEK!",Toast.LENGTH_LONG).show();

        return true;
    }
    //----------------------------------------------------------------------------------------------


    //Block add_photo, delete, process and rename
    private void deleteDoc(int pos){
        DocumentContent docToDelete = savedDocs.get(pos);
        for (int i = 0; i < docToDelete.getPathToFile().size(); i++){
            File fileToDelete = new File(docToDelete.getPathToFile().get(i));
            boolean deleted = fileToDelete.delete();
        }
        savedDocs.remove(pos);
        adapter.notifyDataSetChanged();
    }


    private void renameDoc(int pos){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Title");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mText = input.getText().toString();
                DocumentContent docToRename = savedDocs.get(currListId);
                docToRename.rename(mText);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void addPhoto(int pos){

    }
    //----------------------------------------------------------------------------------------------

    private void makeRequest(String filepath){
        String server = "sdfa";
        String params = "param=10";
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
        DataOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            File file = new File(filepath);
            FileInputStream stream = new FileInputStream(file);
            URL url = new URL(server);
            HttpURLConnection conn = (HttpURLConnection) url.getContent();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
