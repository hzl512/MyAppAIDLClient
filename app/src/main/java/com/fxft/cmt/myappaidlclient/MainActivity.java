package com.fxft.cmt.myappaidlclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fxft.cmt.myappaidlserver.StudentAidl;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ServiceConnectionImpl mServiceConnectionImpl;
    private StudentAidl mStudentAidl;
    EditText textNo;
    TextView textShowNo;
    Button btnQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnQuery = (Button) findViewById(R.id.btnQuery);
        textNo = (EditText) findViewById(R.id.textNo);
        textShowNo = (TextView) findViewById(R.id.textShowNo);
        mServiceConnectionImpl = new ServiceConnectionImpl();
        final Intent eintent = new Intent(createExplicitFromImplicitIntent(this,new Intent("student.query")));
        bindService(eintent, mServiceConnectionImpl, BIND_AUTO_CREATE);

        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name;
                try {
                    name = mStudentAidl.getStudent(new Integer(textNo.getText().toString()));
                    textShowNo.setText(name);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class ServiceConnectionImpl implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mStudentAidl = StudentAidl.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mStudentAidl = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServiceConnectionImpl!=null){
            unbindService(mServiceConnectionImpl);
        }
    }

    /***
     * Android L (lollipop, API 21) introduced a new problem when trying to invoke implicit intent,
     * "java.lang.IllegalArgumentException: Service Intent must be explicit"
     *
     * If you are using an implicit intent, and know only 1 target would answer this intent,
     * This method will help you turn the implicit intent into the explicit form.
     *
     * Inspired from SO answer: http://stackoverflow.com/a/26318757/1446466
     * @param context
     * @param implicitIntent - The original implicit intent
     * @return Explicit Intent created from the implicit original intent
     */
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

}
