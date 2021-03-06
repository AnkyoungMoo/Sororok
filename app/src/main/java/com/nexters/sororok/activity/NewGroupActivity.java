package com.nexters.sororok.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.nexters.sororok.R;
import com.nexters.sororok.asynctask.CreateGroupTask;
import com.nexters.sororok.asynctask.GroupCodeTask;
import com.nexters.sororok.model.GroupResponseModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;


//그룹 생성 화면
public class NewGroupActivity extends AppCompatActivity {

    private static final int REQUEST_USER_GALLERY = 1;
    private ImageView imgGroupPhoto;
    private EditText groupName, groupExplain;
    private TextView textGroupCode;
    private TextView completeBtn;
    private Button backBtn;
    private String photoPath = null;
    private String groupCode;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        initComponent();
        getGroupCode();
        imgGroupPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(NewGroupActivity.this, UserGalleryActivity.class);
                startActivityForResult(i, REQUEST_USER_GALLERY);
            }
        });
       // seekListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_USER_GALLERY:
                    imgGroupPhoto.setBackground(new ShapeDrawable(new OvalShape()));
                    if(Build.VERSION.SDK_INT >= 21) {
                        imgGroupPhoto.setClipToOutline(true);
                        photoPath = data.getStringExtra("photo_path");
                        if(photoPath!=null)
                            Glide.with(this).load(photoPath).into(imgGroupPhoto);
                        break;
                    }

            }
        }
    }


    public void sendGroupInfo(){
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEmpty()){ //전부 채워져있으면
                    callRetrofit();
                    Intent intent = new Intent();
                    intent.putExtra("group_name", groupName.getText().toString());
                    intent.putExtra("group_explain", groupExplain.getText().toString());
                    //intent.putExtra("total_member", getTotal_progress());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

    }

    public boolean isEmpty(){
        if(groupName.getText().toString().length() == 0 || groupExplain.getText().toString().length() == 0){
            Toast.makeText(getApplicationContext(), "모든 정보를 채워주세요", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void initComponent(){
        imgGroupPhoto = findViewById(R.id.img_new_group);
        groupName = findViewById(R.id.edit_group_name); //그룹 이름
        groupExplain = findViewById(R.id.edit_group_explain); //그룹 설명
        completeBtn = findViewById(R.id.txt_new_group_complete);
        sendGroupInfo();
        /*totalMemberSeek = findViewById(R.id.seek_total_member);
        seekValue = findViewById(R.id.txt_seek_value);*/
        textGroupCode = findViewById(R.id.new_group_code);
        backBtn = findViewById(R.id.btn_back);
        linearLayout = findViewById(R.id.layout_copy_group_code);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(groupName.getText().toString().length()>0 && groupExplain.getText().length()>0){
                    ClipboardManager clipboardManager = (ClipboardManager)getApplicationContext().getSystemService(getApplicationContext().CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("group_code", textGroupCode.getText().toString());
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(getApplicationContext(), "그룹코드가 복사되었습니다.",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "빈칸을 모두 채워주세요",Toast.LENGTH_SHORT).show();
                }
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
       /* imgGroupPhoto.setBackground(new ShapeDrawable(new OvalShape()));
        if(Build.VERSION.SDK_INT >= 21) {
            imgGroupPhoto.setClipToOutline(true);
        }*/
    }
/*
    public void seekListener(){
        totalMemberSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                int val = (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax();
                seekValue.setText(progress+"명");
                setTotal_progress(progress); //전체인원
                seekValue.setX(seekBar.getX() + val-18+ seekBar.getThumbOffset() / 2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }*/

    private void getGroupCode() {
        GroupCodeTask groupCodeTask = new GroupCodeTask();

        groupCodeTask.execute();

        try {
            JSONObject jsonObject = new JSONObject(groupCodeTask.get());

            groupCode = jsonObject.getString("code");
            textGroupCode.setText(groupCode);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void callRetrofit() {
        CreateGroupTask createGroupTask = new CreateGroupTask();

        createGroupTask.execute(groupName.getText(),
                groupCode,
                Integer.valueOf(SplashActivity.localId),
                groupExplain.getText(),
                photoPath
        );

        try {
            GroupResponseModel groupResponseModel = createGroupTask.get();
            /* TODO: 뭔가 필요해지면 하자 */
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
