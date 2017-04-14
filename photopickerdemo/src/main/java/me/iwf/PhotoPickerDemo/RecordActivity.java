package me.iwf.PhotoPickerDemo;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.entity.Media;
import me.iwf.photopicker.widget.MovieRecorderView;
import pub.devrel.easypermissions.EasyPermissions;

public class RecordActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private static final int REQUEST_SAVE_AUDIO = 1003;
    private static final int REQUEST_CAMERA_AND_MICROPHONE = 1001;
    private String saveFile;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initRecorderView();
        checkRecorderPermission();
    }

    private void checkRecorderPermission() {

        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.camera_and_microphone_rationale),
                    REQUEST_CAMERA_AND_MICROPHONE, perms);
        }
    }

    private void initRecorderView() {
        MovieRecorderView movieRecorderView = (MovieRecorderView) findViewById(R.id.recorder_view);
        movieRecorderView.setOnRecordFinishListener(onRecordFinishListener);
        movieRecorderView.setRecordMaxTime(getIntent().getIntExtra(MediaStore.EXTRA_DURATION_LIMIT, 0));

        Uri uri = getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT);
        if(uri != null) {
            movieRecorderView.setRecordFile(new File(uri.getPath()));
        }else{
            Toast.makeText(this, R.string.error_params_empty, Toast.LENGTH_LONG).show();
            finish();
        }


    }

    MovieRecorderView.OnRecordFinishListener onRecordFinishListener = new MovieRecorderView.OnRecordFinishListener() {
        @Override
        public void onRecordFinish(File file, int type) {
            saveFile = file.getAbsolutePath();
            if(type == Media.FILE_TYPE_VIDEO) {
                startActivityForResult(VideoPlayActivity.getVideoPlayIntent(RecordActivity.this, file.getPath(), true, true),
                        REQUEST_SAVE_AUDIO);
            }else{
                setResult(RESULT_OK, new Intent()
                        .putExtra(PhotoPicker.EXTRA_MEDIA_TYPE, type)
                        .putExtra(MediaStore.EXTRA_OUTPUT, saveFile));
                finish();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SAVE_AUDIO){
            if(resultCode == RESULT_OK) {
                setResult(RESULT_OK, new Intent()
                        .putExtra(PhotoPicker.EXTRA_MEDIA_TYPE, Media.FILE_TYPE_VIDEO)
                        .putExtra(MediaStore.EXTRA_OUTPUT, saveFile));
                finish();
            }else{
                try{
                    new File(saveFile).delete();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List list) {

        if(requestCode == REQUEST_CAMERA_AND_MICROPHONE){
            Toast.makeText(this, R.string.permission_error, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }
}
