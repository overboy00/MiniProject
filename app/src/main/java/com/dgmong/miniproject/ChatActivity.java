package com.dgmong.miniproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.dgmong.miniproject.data.ChatContract;
import com.dgmong.miniproject.data.NetworkResult;
import com.dgmong.miniproject.data.User;
import com.dgmong.miniproject.gcm.MyGcmListenerService;
import com.dgmong.miniproject.manager.DBManager;
import com.dgmong.miniproject.manager.NetworkManager;
import com.dgmong.miniproject.manager.NetworkRequest;
import com.dgmong.miniproject.request.MessageSendRequest;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.rv_list)
    RecyclerView listView;

    ChatAdapter mAdapter;

    @BindView(R.id.edit_input)
    EditText inputView;

    public static final String EXTRA_USER = "user";
    User user;

    LocalBroadcastManager mLBM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        user = (User) getIntent().getSerializableExtra(EXTRA_USER);

        mAdapter = new ChatAdapter();
        listView.setAdapter(mAdapter);
        listView.setLayoutManager(new LinearLayoutManager(this));
        mLBM = LocalBroadcastManager.getInstance(this);
    }

    @OnClick(R.id.btn_send)
    public void onSend(View view) {
        final String message = inputView.getText().toString();
        MessageSendRequest request = new MessageSendRequest(this, user, message);
        NetworkManager.getInstance().getNetworkData(request, new NetworkManager.OnResultListener<NetworkResult<String>>() {
            @Override
            public void onSuccess(NetworkRequest<NetworkResult<String>> request, NetworkResult<String> result) {
                DBManager.getInstance().addMessage(user, ChatContract.ChatMessage.TYPE_SEND, message);
                updateMessage();
            }

            @Override
            public void onFail(NetworkRequest<NetworkResult<String>> request, int errorCode, String errorMessage, Throwable e) {
            }
        });
//        int type = ChatContract.ChatMessage.TYPE_SEND;
//        switch (typeView.getCheckedRadioButtonId()) {
//            case R.id.radio_send :
//                type = ChatContract.ChatMessage.TYPE_SEND;
//                break;
//            case R.id.radio_receive :
//                type = ChatContract.ChatMessage.TYPE_RECEIVE;
//                break;
//        }
//        DBManager.getInstance().addMessage(user, type, message);
//
//        updateMessage();
    }

    private void updateMessage() {
        Cursor c = DBManager.getInstance().getChatMessage(user);
        mAdapter.changeCursor(c);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateMessage();
        mLBM.registerReceiver(mReceiver, new IntentFilter(MyGcmListenerService.ACTION_CHAT));
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            User u = (User) intent.getSerializableExtra(MyGcmListenerService.EXTRA_CHAT_USER);
            if (u.getId() == user.getId()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateMessage();
                    }
                });
                intent.putExtra(MyGcmListenerService.EXTRA_RESULT, true);
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.changeCursor(null);
        mLBM.unregisterReceiver(mReceiver);
    }
}
