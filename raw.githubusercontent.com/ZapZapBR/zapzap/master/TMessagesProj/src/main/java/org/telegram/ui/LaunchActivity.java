/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.objects.MessageObject;
import org.telegram.ui.Views.BaseFragment;
import org.telegram.ui.Views.NotificationView;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.arellomobile.android.push.BasePushMessageReceiver;
import com.arellomobile.android.push.PushManager;
import com.arellomobile.android.push.utils.RegisterBroadcastReceiver;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.Views.PausableActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;

import com.google.android.gms.ads.*;
import com.google.android.gms.ads.AdRequest;

import com.google.analytics.tracking.android.EasyTracker;

public class LaunchActivity extends ActionBarActivity implements NotificationCenter.NotificationCenterDelegate, MessagesActivity.MessagesActivityDelegate {
    private boolean finished = false;
    private NotificationView notificationView;
    private Uri photoPath = null;
    private String videoPath = null;
    private String sendingText = null;
    private String documentPath = null;
    private ArrayList<Uri> imagesPathArray = null;
    private ArrayList<String> documentsPathArray = null;
    private ArrayList<TLRPC.User> contactsToSend = null;
    private int currentConnectionState;
    private View statusView;
    private View backStatusButton;
    private View statusBackground;
    private TextView statusText;
    private View containerView;
    public String anuncio = "S";
    private AdView adView = null;
    public  String nomearquivo = "zap";
    public int clickAnuncio = 1;
    public String anuncioAtual;
    public String ModoDebug = "N";
    public static String ADMOBID = "your-id-admob-banner";

    //Registration receiver
    BroadcastReceiver mBroadcastReceiver = new RegisterBroadcastReceiver()
    {
        @Override
        public void onRegisterActionReceive(Context context, Intent intent)
        {
            checkMessage(intent);
        }
    };

    //Push message receiver
    private BroadcastReceiver mReceiver = new BasePushMessageReceiver()
    {
        @Override
        protected void onMessageReceive(Intent intent)
        {
            //JSON_DATA_KEY contains JSON payload of push notification.
            showMessage(intent.getExtras().getString(JSON_DATA_KEY));

        }
    };

    //Registration of the receivers
    public void registerReceivers()
    {
        IntentFilter intentFilter = new IntentFilter(getPackageName() + ".action.PUSH_MESSAGE_RECEIVE");

        registerReceiver(mReceiver, intentFilter);

        registerReceiver(mBroadcastReceiver, new IntentFilter(getPackageName() + "." + PushManager.REGISTER_BROAD_CAST_ACTION));
    }

    public void unregisterReceivers()
    {
        //Unregister receivers on pause
        try
        {
            unregisterReceiver(mReceiver);
        }
        catch (Exception e)
        {
            // pass.
        }

        try
        {
            unregisterReceiver(mBroadcastReceiver);
        }
        catch (Exception e)
        {
            //pass through
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceivers();
        PushManager pushManager = PushManager.getInstance(this);
        try {
            pushManager.onStartup(this);
        }
        catch(Exception e)
        {
        }

        pushManager.registerForPushNotifications();
        checkMessage(getIntent());

        if(anuncio=="S") {
            adView = new AdView(this);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(ADMOBID);

        }

        ApplicationLoader.postInitApplication();

        this.setTheme(R.style.Theme_TMessages);
        getWindow().setBackgroundDrawableResource(R.drawable.transparent);
        getWindow().setFormat(PixelFormat.RGB_565);

        if (!UserConfig.clientActivated) {
            Intent intent = getIntent();
            if (intent != null && intent.getAction() != null && (Intent.ACTION_SEND.equals(intent.getAction()) || intent.getAction().equals(Intent.ACTION_SEND_MULTIPLE))) {
                finish();
                return;
            }
            Intent intent2 = new Intent(this, IntroActivity.class);
            startActivity(intent2);
            finish();
            return;
        }

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            Utilities.statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        NotificationCenter.getInstance().postNotificationName(702, this);
        currentConnectionState = ConnectionsManager.getInstance().connectionState;
        for (BaseFragment fragment : ApplicationLoader.fragmentsStack) {
            if (fragment.fragmentView != null) {
                ViewGroup parent = (ViewGroup)fragment.fragmentView.getParent();
                if (parent != null) {
                    parent.removeView(fragment.fragmentView);
                }
                fragment.fragmentView = null;
            }
            fragment.parentActivity = this;
        }

        int anuncioB;
        anuncioB = Integer.parseInt(readFromFile("N"));
        if (anuncioB >= clickAnuncio) {
            anuncioAtual = "TelaSEM";
            setContentView(R.layout.application_layout_2);
        }else {
            setContentView(R.layout.application_layout);
            anuncioAtual = "TelaCOM";
        }

        NotificationCenter.getInstance().addObserver(this, 1234);
        NotificationCenter.getInstance().addObserver(this, 658);
        NotificationCenter.getInstance().addObserver(this, 701);
        NotificationCenter.getInstance().addObserver(this, 702);
        NotificationCenter.getInstance().addObserver(this, 703);
        NotificationCenter.getInstance().addObserver(this, GalleryImageViewer.needShowAllMedia);
        getSupportActionBar().setLogo(R.drawable.ab_icon_fixed2);

        statusView = getLayoutInflater().inflate(R.layout.updating_state_layout, null);
        statusBackground = statusView.findViewById(R.id.back_button_background);
        backStatusButton = statusView.findViewById(R.id.back_button);
        containerView = findViewById(R.id.container);
        statusText = (TextView)statusView.findViewById(R.id.status_text);
        statusBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ApplicationLoader.fragmentsStack.size() > 1) {
                    onBackPressed();
                }
            }
        });

        if (ApplicationLoader.fragmentsStack.isEmpty()) {
            MessagesActivity fragment = new MessagesActivity();
            fragment.onFragmentCreate();
            ApplicationLoader.fragmentsStack.add(fragment);

            try {
                if (savedInstanceState != null) {
                    String fragmentName = savedInstanceState.getString("fragment");
                    if (fragmentName != null) {
                        Bundle args = savedInstanceState.getBundle("args");
                        if (fragmentName.equals("chat")) {
                            if (args != null) {
                                ChatActivity chat = new ChatActivity();
                                chat.setArguments(args);
                                if (chat.onFragmentCreate()) {
                                    ApplicationLoader.fragmentsStack.add(chat);
                                    chat.restoreSelfArgs(savedInstanceState);
                                }
                            }
                        } else if (fragmentName.equals("settings")) {
                            SettingsActivity settings = new SettingsActivity();
                            settings.onFragmentCreate();
                            settings.restoreSelfArgs(savedInstanceState);
                            ApplicationLoader.fragmentsStack.add(settings);
                        } else if (fragmentName.equals("group")) {
                            if (args != null) {
                                GroupCreateFinalActivity group = new GroupCreateFinalActivity();
                                group.setArguments(args);
                                if (group.onFragmentCreate()) {
                                    group.restoreSelfArgs(savedInstanceState);
                                    ApplicationLoader.fragmentsStack.add(group);
                                }
                            }
                        } else if (fragmentName.equals("wallpapers")) {
                            SettingsWallpapersActivity settings = new SettingsWallpapersActivity();
                            settings.onFragmentCreate();
                            settings.restoreSelfArgs(savedInstanceState);
                            ApplicationLoader.fragmentsStack.add(settings);
                        }
                    }
                }
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
        handleIntent(getIntent(), false, savedInstanceState != null);
        if(anuncio=="S") {

            LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
            layout.addView(adView);
            AdRequest adRequestB = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("INSERT_YOUR_HASHED_DEVICE_ID_HERE")
                    .build();
            adView.loadAd(adRequestB);

        }
        if(anuncio=="S") {
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdOpened() {
                    int o = Integer.parseInt(readFromFile("N")) + 1;
                    String oo = String.valueOf(o);
                    writeToFile(oo);
                    finish();
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void prepareForHideShowActionBar() {
        try {
            Class firstClass = getSupportActionBar().getClass();
            Class aClass = firstClass.getSuperclass();
            if (aClass == android.support.v7.app.ActionBar.class) {
                Method method = firstClass.getDeclaredMethod("setShowHideAnimationEnabled", boolean.class);
                method.invoke(getSupportActionBar(), false);
            } else {
                Field field = aClass.getDeclaredField("mActionBar");
                field.setAccessible(true);
                Method method = field.get(getSupportActionBar()).getClass().getDeclaredMethod("setShowHideAnimationEnabled", boolean.class);
                method.invoke(field.get(getSupportActionBar()), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showActionBar() {
        prepareForHideShowActionBar();
        getSupportActionBar().show();
    }

    public void hideActionBar() {
        prepareForHideShowActionBar();
        getSupportActionBar().hide();
    }

    private void handleIntent(Intent intent, boolean isNew, boolean restore) {
        boolean pushOpened = false;

        Integer push_user_id = 0;
        Integer push_chat_id = 0;
        Integer push_enc_id = 0;
        Integer open_settings = 0;

        photoPath = null;
        videoPath = null;
        sendingText = null;
        documentPath = null;
        imagesPathArray = null;
        documentsPathArray = null;

        if (intent != null && intent.getAction() != null && !restore) {
            if (Intent.ACTION_SEND.equals(intent.getAction())) {
                boolean error = false;
                String type = intent.getType();
                if (type != null && type.equals("text/plain")) {
                    String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                    String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);

                    if (text != null && text.length() != 0) {
                        if ((text.startsWith("http://") || text.startsWith("https://")) && subject != null && subject.length() != 0) {
                            text = subject + "\n" + text;
                        }
                        sendingText = text;
                    } else {
                        error = true;
                    }
                } else if (type != null && type.equals(ContactsContract.Contacts.CONTENT_VCARD_TYPE)) {
                    try {
                        Uri uri = (Uri)intent.getExtras().get(Intent.EXTRA_STREAM);
                        if (uri != null) {
                            ContentResolver cr = getContentResolver();
                            InputStream stream = cr.openInputStream(uri);

                            String name = null;
                            String nameEncoding = null;
                            String nameCharset = null;
                            ArrayList<String> phones = new ArrayList<String>();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                            String line = null;
                            while ((line = bufferedReader.readLine()) != null) {
                                String[] args = line.split(":");
                                if (args.length != 2) {
                                    continue;
                                }
                                if (args[0].startsWith("FN")) {
                                    String[] params = args[0].split(";");
                                    for (String param : params) {
                                        String[] args2 = param.split("=");
                                        if (args2.length != 2) {
                                            continue;
                                        }
                                        if (args2[0].equals("CHARSET")) {
                                            nameCharset = args2[1];
                                        } else if (args2[0].equals("ENCODING")) {
                                            nameEncoding = args2[1];
                                        }
                                    }
                                    name = args[1];
                                    if (nameEncoding != null && nameEncoding.equalsIgnoreCase("QUOTED-PRINTABLE")) {
                                        while (name.endsWith("=") && nameEncoding != null) {
                                            name = name.substring(0, name.length() - 1);
                                            line = bufferedReader.readLine();
                                            if (line == null) {
                                                break;
                                            }
                                            name += line;
                                        }
                                        byte[] bytes = Utilities.decodeQuotedPrintable(name.getBytes());
                                        if (bytes != null && bytes.length != 0) {
                                            String decodedName = new String(bytes, nameCharset);
                                            if (decodedName != null) {
                                                name = decodedName;
                                            }
                                        }
                                    }
                                } else if (args[0].startsWith("TEL")) {
                                    String phone = PhoneFormat.stripExceptNumbers(args[1], true);
                                    if (phone.length() > 0) {
                                        phones.add(phone);
                                    }
                                }
                            }
                            if (name != null && !phones.isEmpty()) {
                                contactsToSend = new ArrayList<TLRPC.User>();
                                for (String phone : phones) {
                                    TLRPC.User user = new TLRPC.TL_userContact();
                                    user.phone = phone;
                                    user.first_name = name;
                                    user.last_name = "";
                                    user.id = 0;
                                    contactsToSend.add(user);
                                }
                            }
                        } else {
                            error = true;
                        }
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                        error = true;
                    }
                } else {
                    Parcelable parcelable = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (parcelable == null) {
                        return;
                    }
                    String path = null;
                    if (!(parcelable instanceof Uri)) {
                        parcelable = Uri.parse(parcelable.toString());
                    }
                    if (parcelable != null && type != null && type.startsWith("image/")) {
                        if (type.equals("image/gif")) {
                            try {
                                documentPath = Utilities.getPath((Uri)parcelable);
                            } catch (Exception e) {
                                FileLog.e("tmessages", e);
                            }
                            if (documentPath == null) {
                                photoPath = (Uri) parcelable;
                            }
                        } else {
                            photoPath = (Uri) parcelable;
                        }
                    } else {
                        path = Utilities.getPath((Uri)parcelable);
                        if (path != null) {
                            if (path.startsWith("file:")) {
                                path = path.replace("file://", "");
                            }
                            if (type != null && type.startsWith("video/")) {
                                videoPath = path;
                            } else {
                                documentPath = path;
                            }
                        } else {
                            error = true;
                        }
                    }
                    if (error) {
                        Toast.makeText(this, "Unsupported content", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (intent.getAction().equals(Intent.ACTION_SEND_MULTIPLE)) {
                boolean error = false;
                try {
                    ArrayList<Parcelable> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    String type = intent.getType();
                    if (uris != null) {
                        if (type != null && type.startsWith("image/")) {
                            for (Parcelable parcelable : uris) {
                                if (!(parcelable instanceof Uri)) {
                                    parcelable = Uri.parse(parcelable.toString());
                                }
                                if (type.equals("image/gif")) {
                                    if (documentsPathArray == null) {
                                        documentsPathArray = new ArrayList<String>();
                                    }
                                    try {
                                        documentsPathArray.add(Utilities.getPath((Uri) parcelable));
                                    } catch (Exception e) {
                                        FileLog.e("tmessages", e);
                                    }
                                } else {
                                    if (imagesPathArray == null) {
                                        imagesPathArray = new ArrayList<Uri>();
                                    }
                                    imagesPathArray.add((Uri) parcelable);
                                }
                            }
                        } else {
                            for (Parcelable parcelable : uris) {
                                if (!(parcelable instanceof Uri)) {
                                    parcelable = Uri.parse(parcelable.toString());
                                }
                                String path = Utilities.getPath((Uri) parcelable);
                                if (path != null) {
                                    if (path.startsWith("file:")) {
                                        path = path.replace("file://", "");
                                    }
                                    if (documentsPathArray == null) {
                                        documentsPathArray = new ArrayList<String>();
                                    }
                                    documentsPathArray.add(path);
                                }
                            }
                        }
                    } else {
                        error = true;
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                    error = true;
                }
                if (error) {
                    Toast.makeText(this, "Unsupported content", Toast.LENGTH_SHORT).show();
                }
            } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                try {
                    Cursor cursor = getContentResolver().query(intent.getData(), null, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            int userId = cursor.getInt(cursor.getColumnIndex("DATA4"));
                            NotificationCenter.getInstance().postNotificationName(MessagesController.closeChats);
                            push_user_id = userId;
                        }
                        cursor.close();
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            } else if (intent.getAction().equals("org.telegram.messenger.OPEN_ACCOUNT")) {
                open_settings = 1;
            }
        }

        if (getIntent().getAction() != null && getIntent().getAction().startsWith("com.tmessages.openchat") && (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0 && !restore) {
            int chatId = getIntent().getIntExtra("chatId", 0);
            int userId = getIntent().getIntExtra("userId", 0);
            int encId = getIntent().getIntExtra("encId", 0);
            if (chatId != 0) {
                TLRPC.Chat chat = MessagesController.getInstance().chats.get(chatId);
                if (chat != null) {
                    NotificationCenter.getInstance().postNotificationName(MessagesController.closeChats);
                    push_chat_id = chatId;
                }
            } else if (userId != 0) {
                TLRPC.User user = MessagesController.getInstance().users.get(userId);
                if (user != null) {
                    NotificationCenter.getInstance().postNotificationName(MessagesController.closeChats);
                    push_user_id = userId;
                }
            } else if (encId != 0) {
                TLRPC.EncryptedChat chat = MessagesController.getInstance().encryptedChats.get(encId);
                if (chat != null) {
                    NotificationCenter.getInstance().postNotificationName(MessagesController.closeChats);
                    push_enc_id = encId;
                }
            }
        }

        if (push_user_id != 0) {
            if (push_user_id == UserConfig.clientUserId) {
                open_settings = 1;
            } else {
                ChatActivity fragment = new ChatActivity();
                Bundle bundle = new Bundle();
                bundle.putInt("user_id", push_user_id);
                fragment.setArguments(bundle);
                if (fragment.onFragmentCreate()) {
                    pushOpened = true;
                    ApplicationLoader.fragmentsStack.add(fragment);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, "chat" + Math.random()).commitAllowingStateLoss();
                }
            }
        } else if (push_chat_id != 0) {
            ChatActivity fragment = new ChatActivity();
            Bundle bundle = new Bundle();
            bundle.putInt("chat_id", push_chat_id);
            fragment.setArguments(bundle);
            if (fragment.onFragmentCreate()) {
                pushOpened = true;
                ApplicationLoader.fragmentsStack.add(fragment);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, "chat" + Math.random()).commitAllowingStateLoss();
            }
        }  else if (push_enc_id != 0) {
            ChatActivity fragment = new ChatActivity();
            Bundle bundle = new Bundle();
            bundle.putInt("enc_id", push_enc_id);
            fragment.setArguments(bundle);
            if (fragment.onFragmentCreate()) {
                pushOpened = true;
                ApplicationLoader.fragmentsStack.add(fragment);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, "chat" + Math.random()).commitAllowingStateLoss();
            }
        }
        if (videoPath != null || photoPath != null || sendingText != null || documentPath != null || documentsPathArray != null || imagesPathArray != null || contactsToSend != null) {
            MessagesActivity fragment = new MessagesActivity();
            fragment.selectAlertString = R.string.ForwardMessagesTo;
            fragment.selectAlertStringDesc = "ForwardMessagesTo";
            fragment.animationType = 1;
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            fragment.setArguments(args);
            fragment.delegate = this;
            ApplicationLoader.fragmentsStack.add(fragment);
            fragment.onFragmentCreate();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, fragment.getTag()).commitAllowingStateLoss();
            pushOpened = true;
        }
        if (open_settings != 0) {
            SettingsActivity fragment = new SettingsActivity();
            ApplicationLoader.fragmentsStack.add(fragment);
            fragment.onFragmentCreate();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, "settings").commitAllowingStateLoss();
            pushOpened = true;
        }
        if (!pushOpened && !isNew) {
            BaseFragment fragment = ApplicationLoader.fragmentsStack.get(ApplicationLoader.fragmentsStack.size() - 1);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, fragment.getTag()).commitAllowingStateLoss();
        }

        getIntent().setAction(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent, true, false);
    }

    @Override
    public void didSelectDialog(MessagesActivity messageFragment, long dialog_id) {
        if (dialog_id != 0) {
            int lower_part = (int)dialog_id;

            ChatActivity fragment = new ChatActivity();
            Bundle bundle = new Bundle();
            if (lower_part != 0) {
                if (lower_part > 0) {
                    NotificationCenter.getInstance().postNotificationName(MessagesController.closeChats);
                    bundle.putInt("user_id", lower_part);
                    fragment.setArguments(bundle);
                    fragment.scrollToTopOnResume = true;
                    presentFragment(fragment, "chat" + Math.random(), true, false);
                } else if (lower_part < 0) {
                    NotificationCenter.getInstance().postNotificationName(MessagesController.closeChats);
                    bundle.putInt("chat_id", -lower_part);
                    fragment.setArguments(bundle);
                    fragment.scrollToTopOnResume = true;
                    presentFragment(fragment, "chat" + Math.random(), true, false);
                }
            } else {
                NotificationCenter.getInstance().postNotificationName(MessagesController.closeChats);
                int chat_id = (int)(dialog_id >> 32);
                bundle.putInt("enc_id", chat_id);
                fragment.setArguments(bundle);
                fragment.scrollToTopOnResume = true;
                presentFragment(fragment, "chat" + Math.random(), true, false);
            }
            if (photoPath != null) {
                fragment.processSendingPhoto(null, photoPath);
            }
            if (videoPath != null) {
                fragment.processSendingVideo(videoPath);
            }
            if (sendingText != null) {
                fragment.processSendingText(sendingText);
            }
            if (documentPath != null) {
                fragment.processSendingDocument(documentPath);
            }
            if (imagesPathArray != null) {
                for (Uri path : imagesPathArray) {
                    fragment.processSendingPhoto(null, path);
                }
            }
            if (documentsPathArray != null) {
                for (String path : documentsPathArray) {
                    fragment.processSendingDocument(path);
                }
            }
            if (contactsToSend != null && !contactsToSend.isEmpty()) {
                for (TLRPC.User user : contactsToSend) {
                    MessagesController.getInstance().sendMessage(user, dialog_id);
                }
            }
            photoPath = null;
            videoPath = null;
            sendingText = null;
            documentPath = null;
            imagesPathArray = null;
            documentsPathArray = null;
            contactsToSend = null;
        }
    }

    private void checkForCrashes() {
        CrashManager.register(this, BuildVars.HOCKEY_APP_HASH);
    }

    private void checkForUpdates() {
        if (BuildVars.DEBUG_VERSION) {
            UpdateManager.register(this, BuildVars.HOCKEY_APP_HASH);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ApplicationLoader.fragmentsStack.size() != 0) {
            BaseFragment fragment = ApplicationLoader.fragmentsStack.get(ApplicationLoader.fragmentsStack.size() - 1);
            fragment.onActivityResultFragment(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null) {
            adView.pause();
        }
        ApplicationLoader.lastPauseTime = System.currentTimeMillis();
        if (notificationView != null) {
            notificationView.hide(false);
        }
        View focusView = getCurrentFocus();
        if (focusView instanceof EditText) {
            focusView.clearFocus();
        }
        unregisterReceivers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
        processOnFinish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adView != null) {
            adView.resume();
        }


        if (notificationView == null && getLayoutInflater() != null) {
            notificationView = (NotificationView) getLayoutInflater().inflate(R.layout.notification_layout, null);
        }
        fixLayout();
        checkForCrashes();
        checkForUpdates();
        ApplicationLoader.resetLastPauseTime();
        supportInvalidateOptionsMenu();
        updateActionBar();
        try {
            NotificationManager mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(1);
            MessagesController.getInstance().currentPushMessage = null;
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        registerReceivers();
        readFromFile("S");
    }

    private void processOnFinish() {
        if (finished) {
            return;
        }
        finished = true;
        NotificationCenter.getInstance().removeObserver(this, 1234);
        NotificationCenter.getInstance().removeObserver(this, 658);
        NotificationCenter.getInstance().removeObserver(this, 701);
        NotificationCenter.getInstance().removeObserver(this, 702);
        NotificationCenter.getInstance().removeObserver(this, 703);
        NotificationCenter.getInstance().removeObserver(this, GalleryImageViewer.needShowAllMedia);
        if (notificationView != null) {
            notificationView.hide(false);
            notificationView.destroy();
            notificationView = null;
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Utilities.checkDisplaySize();
        fixLayout();
    }

    private void fixLayout() {
        if (containerView != null) {
            ViewTreeObserver obs = containerView.getViewTreeObserver();
            obs.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                    int rotation = manager.getDefaultDisplay().getRotation();

                    int height;
                    int currentActionBarHeight = getSupportActionBar().getHeight();
                    if (currentActionBarHeight != Utilities.dp(48) && currentActionBarHeight != Utilities.dp(40)) {
                        height = currentActionBarHeight;
                    } else {
                        height = Utilities.dp(48);
                        if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90) {
                            height = Utilities.dp(40);
                        }
                    }

                    if (notificationView != null) {
                        notificationView.applyOrientationPaddings(rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90, height);
                    }

                    if (Build.VERSION.SDK_INT < 16) {
                        containerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        containerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void didReceivedNotification(int id, Object... args) {
        if (id == 1234) {
            for (BaseFragment fragment : ApplicationLoader.fragmentsStack) {
                fragment.onFragmentDestroy();
            }
            ApplicationLoader.fragmentsStack.clear();
            Intent intent2 = new Intent(this, IntroActivity.class);
            startActivity(intent2);
            processOnFinish();
            finish();
        } else if (id == GalleryImageViewer.needShowAllMedia) {
            long dialog_id = (Long)args[0];
            MediaActivity fragment = new MediaActivity();
            Bundle bundle = new Bundle();
            if (dialog_id != 0) {
                bundle.putLong("dialog_id", dialog_id);
                fragment.setArguments(bundle);
                presentFragment(fragment, "media_" + dialog_id, false);
            }
        } else if (id == 658) {
            Integer push_user_id = (Integer)NotificationCenter.getInstance().getFromMemCache("push_user_id", 0);
            Integer push_chat_id = (Integer)NotificationCenter.getInstance().getFromMemCache("push_chat_id", 0);
            Integer push_enc_id = (Integer)NotificationCenter.getInstance().getFromMemCache("push_enc_id", 0);

            if (push_user_id != 0) {
                NotificationCenter.getInstance().postNotificationName(MessagesController.closeChats);
                ChatActivity fragment = new ChatActivity();
                Bundle bundle = new Bundle();
                bundle.putInt("user_id", push_user_id);
                fragment.setArguments(bundle);
                if (fragment.onFragmentCreate()) {
                    if (ApplicationLoader.fragmentsStack.size() > 0) {
                        BaseFragment lastFragment = ApplicationLoader.fragmentsStack.get(ApplicationLoader.fragmentsStack.size() - 1);
                        lastFragment.willBeHidden();
                    }
                    ApplicationLoader.fragmentsStack.add(fragment);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, "chat" + Math.random()).commitAllowingStateLoss();
                }
            } else if (push_chat_id != 0) {
                NotificationCenter.getInstance().postNotificationName(MessagesController.closeChats);
                ChatActivity fragment = new ChatActivity();
                Bundle bundle = new Bundle();
                bundle.putInt("chat_id", push_chat_id);
                fragment.setArguments(bundle);
                if (fragment.onFragmentCreate()) {
                    if (ApplicationLoader.fragmentsStack.size() > 0) {
                        BaseFragment lastFragment = ApplicationLoader.fragmentsStack.get(ApplicationLoader.fragmentsStack.size() - 1);
                        lastFragment.willBeHidden();
                    }
                    ApplicationLoader.fragmentsStack.add(fragment);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, "chat" + Math.random()).commitAllowingStateLoss();
                }
            }  else if (push_enc_id != 0) {
                NotificationCenter.getInstance().postNotificationName(MessagesController.closeChats);
                ChatActivity fragment = new ChatActivity();
                Bundle bundle = new Bundle();
                bundle.putInt("enc_id", push_enc_id);
                fragment.setArguments(bundle);
                if (fragment.onFragmentCreate()) {
                    if (ApplicationLoader.fragmentsStack.size() > 0) {
                        BaseFragment lastFragment = ApplicationLoader.fragmentsStack.get(ApplicationLoader.fragmentsStack.size() - 1);
                        lastFragment.willBeHidden();
                    }
                    ApplicationLoader.fragmentsStack.add(fragment);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, "chat" + Math.random()).commitAllowingStateLoss();
                }
            }
        } else if (id == 701) {
            if (notificationView != null) {
                MessageObject message = (MessageObject)args[0];
                notificationView.show(message);
            }
        } else if (id == 702) {
            if (args[0] != this) {
                processOnFinish();
            }
        } else if (id == 703) {
            int state = (Integer)args[0];
            if (currentConnectionState != state) {
                FileLog.e("tmessages", "switch to state " + state);
                currentConnectionState = state;
                updateActionBar();
            }
        }
    }

    public void fixBackButton() {
        if(android.os.Build.VERSION.SDK_INT == 19) {
            //workaround for back button dissapear
            try {
                Class firstClass = getSupportActionBar().getClass();
                Class aClass = firstClass.getSuperclass();
                if (aClass == android.support.v7.app.ActionBar.class) {

                } else {
                    Field field = aClass.getDeclaredField("mActionBar");
                    field.setAccessible(true);
                    android.app.ActionBar bar = (android.app.ActionBar)field.get(getSupportActionBar());

                    field = bar.getClass().getDeclaredField("mActionView");
                    field.setAccessible(true);
                    View v = (View)field.get(bar);
                    aClass = v.getClass();

                    field = aClass.getDeclaredField("mHomeLayout");
                    field.setAccessible(true);
                    v = (View)field.get(v);
                    v.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        BaseFragment currentFragment = null;
        if (!ApplicationLoader.fragmentsStack.isEmpty()) {
            currentFragment = ApplicationLoader.fragmentsStack.get(ApplicationLoader.fragmentsStack.size() - 1);
        }
        boolean canApplyLoading = true;
        if (currentFragment != null && (currentConnectionState == 0 || !currentFragment.canApplyUpdateStatus() || statusView == null)) {
            currentFragment.applySelfActionBar();
            canApplyLoading = false;
        }
        if (canApplyLoading) {
            if (statusView != null) {
                statusView.setVisibility(View.VISIBLE);
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setDisplayShowHomeEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setSubtitle(null);

                if (ApplicationLoader.fragmentsStack.size() > 1) {
                    backStatusButton.setVisibility(View.VISIBLE);
                    statusBackground.setEnabled(true);
                } else {
                    backStatusButton.setVisibility(View.GONE);
                    statusBackground.setEnabled(false);
                }

                if (currentConnectionState == 1) {
                    statusText.setText(getString(R.string.WaitingForNetwork));
                } else if (currentConnectionState == 2) {
                    statusText.setText(getString(R.string.Connecting));
                } else if (currentConnectionState == 3) {
                    statusText.setText(getString(R.string.Updating));
                }
                if (actionBar.getCustomView() != statusView) {
                    actionBar.setCustomView(statusView);
                }

                try {
                    if (statusView.getLayoutParams() instanceof android.support.v7.app.ActionBar.LayoutParams) {
                        android.support.v7.app.ActionBar.LayoutParams statusParams = (android.support.v7.app.ActionBar.LayoutParams)statusView.getLayoutParams();
                        statusText.measure(View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.AT_MOST));
                        statusParams.width = (statusText.getMeasuredWidth() + Utilities.dp(54));
                        if (statusParams.height == 0) {
                            statusParams.height = actionBar.getHeight();
                        }
                        if (statusParams.width <= 0) {
                            statusParams.width = Utilities.dp(100);
                        }
                        statusParams.topMargin = 0;
                        statusParams.leftMargin = 0;
                        statusView.setLayoutParams(statusParams);
                    } else if (statusView.getLayoutParams() instanceof android.app.ActionBar.LayoutParams) {
                        android.app.ActionBar.LayoutParams statusParams = (android.app.ActionBar.LayoutParams)statusView.getLayoutParams();
                        statusText.measure(View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.AT_MOST));
                        statusParams.width = (statusText.getMeasuredWidth() + Utilities.dp(54));
                        if (statusParams.height == 0) {
                            statusParams.height = actionBar.getHeight();
                        }
                        if (statusParams.width <= 0) {
                            statusParams.width = Utilities.dp(100);
                        }
                        statusParams.topMargin = 0;
                        statusParams.leftMargin = 0;
                        statusView.setLayoutParams(statusParams);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void presentFragment(BaseFragment fragment, String tag, boolean bySwipe) {
        presentFragment(fragment, tag, false, bySwipe);
    }

    public void presentFragment(BaseFragment fragment, String tag, boolean removeLast, boolean bySwipe) {
        if (getCurrentFocus() != null) {
            Utilities.hideKeyboard(getCurrentFocus());
        }
        if (!fragment.onFragmentCreate()) {
            return;
        }
        BaseFragment current = null;
        if (!ApplicationLoader.fragmentsStack.isEmpty()) {
            current = ApplicationLoader.fragmentsStack.get(ApplicationLoader.fragmentsStack.size() - 1);
        }
        if (current != null) {
            current.willBeHidden();
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fTrans = fm.beginTransaction();
        if (removeLast && current != null) {
            ApplicationLoader.fragmentsStack.remove(ApplicationLoader.fragmentsStack.size() - 1);
            current.onFragmentDestroy();
        }
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        boolean animations = preferences.getBoolean("view_animations", true);
        if (animations) {
            if (bySwipe) {
                fTrans.setCustomAnimations(R.anim.slide_left, R.anim.no_anim);
            } else {
                fTrans.setCustomAnimations(R.anim.scale_in, R.anim.no_anim);
            }
        }
        try {
            fTrans.replace(R.id.container, fragment, tag);
            fTrans.commitAllowingStateLoss();
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        ApplicationLoader.fragmentsStack.add(fragment);
    }

    public void removeFromStack(BaseFragment fragment) {
        ApplicationLoader.fragmentsStack.remove(fragment);
        fragment.onFragmentDestroy();
    }

    public void finishFragment(boolean bySwipe) {
        if (getCurrentFocus() != null) {
            Utilities.hideKeyboard(getCurrentFocus());
        }
        if (ApplicationLoader.fragmentsStack.size() < 2) {
            for (BaseFragment fragment : ApplicationLoader.fragmentsStack) {
                fragment.onFragmentDestroy();
            }
            ApplicationLoader.fragmentsStack.clear();
            MessagesActivity fragment = new MessagesActivity();
            fragment.onFragmentCreate();
            ApplicationLoader.fragmentsStack.add(fragment);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, "chats").commitAllowingStateLoss();
            return;
        }
        BaseFragment fragment = ApplicationLoader.fragmentsStack.get(ApplicationLoader.fragmentsStack.size() - 1);
        fragment.onFragmentDestroy();
        BaseFragment prev = ApplicationLoader.fragmentsStack.get(ApplicationLoader.fragmentsStack.size() - 2);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fTrans = fm.beginTransaction();
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        boolean animations = preferences.getBoolean("view_animations", true);
        if (animations) {
            if (bySwipe) {
                fTrans.setCustomAnimations(R.anim.no_anim_show, R.anim.slide_right_away);
            } else {
                fTrans.setCustomAnimations(R.anim.no_anim_show, R.anim.scale_out);
            }
        }
        fTrans.replace(R.id.container, prev, prev.getTag());
        fTrans.commitAllowingStateLoss();
        ApplicationLoader.fragmentsStack.remove(ApplicationLoader.fragmentsStack.size() - 1);
    }

    @Override
    public void onBackPressed() {
        if (ApplicationLoader.fragmentsStack.size() == 1) {
            ApplicationLoader.fragmentsStack.get(0).onFragmentDestroy();
            ApplicationLoader.fragmentsStack.clear();
            processOnFinish();
            finish();
            return;
        }
        if (!ApplicationLoader.fragmentsStack.isEmpty()) {
            BaseFragment lastFragment = ApplicationLoader.fragmentsStack.get(ApplicationLoader.fragmentsStack.size() - 1);
            if (lastFragment.onBackPressed()) {
                finishFragment(false);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
            if (!ApplicationLoader.fragmentsStack.isEmpty()) {
                BaseFragment lastFragment = ApplicationLoader.fragmentsStack.get(ApplicationLoader.fragmentsStack.size() - 1);
                Bundle args = lastFragment.getArguments();
                if (lastFragment instanceof ChatActivity && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "chat");
                } else if (lastFragment instanceof SettingsActivity) {
                    outState.putString("fragment", "settings");
                } else if (lastFragment instanceof GroupCreateFinalActivity && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "group");
                } else if (lastFragment instanceof SettingsWallpapersActivity) {
                    outState.putString("fragment", "wallpapers");
                }
                lastFragment.saveSelfArgs(outState);
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private void checkMessage(Intent intent)
    {
        if (null != intent)
        {
            if (intent.hasExtra(PushManager.PUSH_RECEIVE_EVENT))
            {
                showMessage("push message is " + intent.getExtras().getString(PushManager.PUSH_RECEIVE_EVENT));
            }
            else if (intent.hasExtra(PushManager.REGISTER_EVENT))
            {
                showMessage("register");
            }
            else if (intent.hasExtra(PushManager.UNREGISTER_EVENT))
            {
                showMessage("unregister");
            }
            else if (intent.hasExtra(PushManager.REGISTER_ERROR_EVENT))
            {
                showMessage("register error");
            }
            else if (intent.hasExtra(PushManager.UNREGISTER_ERROR_EVENT))
            {
                showMessage("unregister error");
            }

            resetIntentValues();
        }
    }

    /**
     * Will check main Activity intent and if it contains any PushWoosh data, will clear it
     */
    private void resetIntentValues()
    {
        Intent mainAppIntent = getIntent();

        if (mainAppIntent.hasExtra(PushManager.PUSH_RECEIVE_EVENT))
        {
            mainAppIntent.removeExtra(PushManager.PUSH_RECEIVE_EVENT);
        }
        else if (mainAppIntent.hasExtra(PushManager.REGISTER_EVENT))
        {
            mainAppIntent.removeExtra(PushManager.REGISTER_EVENT);
        }
        else if (mainAppIntent.hasExtra(PushManager.UNREGISTER_EVENT))
        {
            mainAppIntent.removeExtra(PushManager.UNREGISTER_EVENT);
        }
        else if (mainAppIntent.hasExtra(PushManager.REGISTER_ERROR_EVENT))
        {
            mainAppIntent.removeExtra(PushManager.REGISTER_ERROR_EVENT);
        }
        else if (mainAppIntent.hasExtra(PushManager.UNREGISTER_ERROR_EVENT))
        {
            mainAppIntent.removeExtra(PushManager.UNREGISTER_ERROR_EVENT);
        }

        setIntent(mainAppIntent);
    }

    private void showMessage(String message)
    {
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
        if(ModoDebug == "S") {
            Toast.makeText(this, anuncioAtual, Toast.LENGTH_LONG).show();
        }
    }

    private void writeToFile(String data) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String dataAtual = sdf.format(new Date());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(nomearquivo+dataAtual, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
        }
    }

    private void gravaTxt(String numero) {
        String o = numero;
        writeToFile(o);
    }

    private String readFromFile(String fecha) {
        String ret = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String dataAtual = sdf.format(new Date());
            InputStream inputStream = openFileInput(nomearquivo+dataAtual);
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            gravaTxt("0");
            ret = String.valueOf("0");
            if(fecha=="S") {
                  finish();
            }
        } catch (IOException e) {
            ret = "0";
        }
        if(ModoDebug == "S") {
            Toast.makeText(this, ret, Toast.LENGTH_LONG).show();
        }
        return ret;
    }
}
