package com.dgmong.miniproject.request;

import android.content.Context;

import com.dgmong.miniproject.Utils;
import com.dgmong.miniproject.data.ChatMessage;
import com.dgmong.miniproject.data.NetworkResult;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * Created by Administrator on 2016-08-23.
 */
public class MessageListRequest extends AbstractRequest<NetworkResult<List<ChatMessage>>> {
    Request mRequest;
    public MessageListRequest(Context context, Date date) {
        String dateString = Utils.convertTimeToString(date);
        HttpUrl url = getBaseUrlBuilder()
                .addPathSegment("messagelist")
                .addQueryParameter("lastDate",dateString)
                .build();
        mRequest = new Request.Builder()
                .url(url)
                .tag(context)
                .build();
    }
    @Override
    protected Type getType() {
        return new TypeToken<NetworkResult<List<ChatMessage>>>(){}.getType();
    }

    @Override
    public Request getRequest() {
        return mRequest;
    }
}
