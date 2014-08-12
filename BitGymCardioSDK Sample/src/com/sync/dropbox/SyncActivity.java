package com.sync.dropbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.activetheoryinc.samplecardioactivity.R;
import com.activetheoryinc.samplecardioactivity.SampleCardioActivity;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.gemapp.db.DBHelper;

public class SyncActivity extends Activity {

    private static final String appKey = "hluwn08daia8j5k";
    private static final String appSecret = "8w64ox89ao81qqh";

    private static final int REQUEST_LINK_TO_DBX = 0;
    private static final int HIDE_PROGRESS = 0;
    private static final int SHOW_PROGRESS = 1;

    private DbxAccountManager mDbxAcctMgr;
    private Button mLinkButton;
    private Button mBackupButton;
    private Button mRestoreButton;
    private Button mStartButton;
    private ProgressBar mProgressBar;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sync_layout);

        mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(),
                appKey, appSecret);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == SHOW_PROGRESS)
                    showProgress();
                else if (msg.what == HIDE_PROGRESS)
                    hideProgress();
            }
        };

        mLinkButton = (Button) findViewById(R.id.link_button);
        mLinkButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (!mDbxAcctMgr.hasLinkedAccount()) {
                    mDbxAcctMgr.startLink(SyncActivity.this,
                            REQUEST_LINK_TO_DBX);
                } else {
                    mDbxAcctMgr.unlink();
                    showUnlinkedView();
                }
            }
        });

        mBackupButton = (Button) findViewById(R.id.backup_button);
        mBackupButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                new Thread(new Runnable() {

                    public void run() {
                        handler.sendEmptyMessage(SHOW_PROGRESS);
                        dbBackup();
                        handler.sendEmptyMessage(HIDE_PROGRESS);
                    }
                }).start();
            }
        });

        mRestoreButton = (Button) findViewById(R.id.restore_button);
        mRestoreButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                new Thread(new Runnable() {

                    public void run() {
                        handler.sendEmptyMessage(SHOW_PROGRESS);
                        dbRestore();
                        handler.sendEmptyMessage(HIDE_PROGRESS);
                    }
                }).start();
            }
        });

        mStartButton = (Button) findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent next = new Intent(SyncActivity.this,
                        SampleCardioActivity.class);
                startActivity(next);
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDbxAcctMgr.hasLinkedAccount()) {
            showLinkedView();
        } else {
            showUnlinkedView();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LINK_TO_DBX) {
            if (resultCode == Activity.RESULT_OK) {
                showLinkedView();
                Toast.makeText(getApplicationContext(),
                        "Successful link to Dropbox", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(SyncActivity.this,
                        "Link to Dropbox failed or was cancelled.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void dbRestore() {
        String LOG_TAG = "debug log";
        try {
            DBHelper dbHelper = new DBHelper(getApplicationContext());
            dbHelper.getWritableDatabase().getPath();
            final String DB_FILE_NAME = dbHelper.getDatabaseName() + ".db";
            final String DB_FILE_PATH = dbHelper.getWritableDatabase()
                    .getPath();
            dbHelper.close();

            DbxPath testPath = new DbxPath(DbxPath.ROOT, DB_FILE_NAME);

            // Create DbxFileSystem for synchronized file access.
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr
                    .getLinkedAccount());
            dbxFs.awaitFirstSync();

            if (dbxFs.exists(testPath)) {
                DbxFile testFile = dbxFs.open(testPath);
                try {
                    File oldFile = new File(DB_FILE_PATH);
                    oldFile.delete();

                    File newFile = new File(DB_FILE_PATH);
                    newFile.createNewFile();

                    InputStream in = testFile.getReadStream();
                    OutputStream out = new FileOutputStream(newFile);
                    byte[] buffer = new byte[1024];
                    while (in.read(buffer) > 0)
                        out.write(buffer);

                    out.flush();
                    out.close();
                    in.close();
                } finally {
                    testFile.close();
                }
            } else
                Log.e(LOG_TAG, "Buckup file does not exist!");

        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage().toString());
        }
    }

    public void dbBackup() {
        String LOG_TAG = "debug log";
        Log.d(LOG_TAG, "Dropbox Sync API Version "
                + DbxAccountManager.SDK_VERSION_NAME + "\n");
        try {
            DBHelper dbHelper = new DBHelper(getApplicationContext());
            dbHelper.getWritableDatabase().getPath();
            final String DB_FILE_NAME = dbHelper.getDatabaseName() + ".db";
            final String DB_FILE_PATH = dbHelper.getWritableDatabase()
                    .getPath();
            dbHelper.close();

            DbxPath testPath = new DbxPath(DbxPath.ROOT, DB_FILE_NAME);

            // Create DbxFileSystem for synchronized file access.
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr
                    .getLinkedAccount());
            dbxFs.awaitFirstSync();

            // Print the contents of the root folder. This will block until we
            // can
            // sync metadata the first time.
            // List<DbxFileInfo> infos = dbxFs.listFolder(DbxPath.ROOT);

            // Create a test file only if it doesn't already exist.
            DbxFile testFile;
            if (!dbxFs.exists(testPath))
                testFile = dbxFs.create(testPath);
            else
                testFile = dbxFs.open(testPath);

            try {
                File file = new File(DB_FILE_PATH);
                testFile.writeFromExistingFile(file, false);
            } finally {
                testFile.close();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage().toString());
        }

    }

    private void showLinkedView() {
        mLinkButton.setText("Unlink to Dropbox");
        mBackupButton.setEnabled(true);
        mRestoreButton.setEnabled(true);
    }

    private void showUnlinkedView() {
        mLinkButton.setText("Link to Dropbox");
        mBackupButton.setEnabled(false);
        mRestoreButton.setEnabled(false);
    }

    private void showProgress() {
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mStartButton.setEnabled(false);
        mBackupButton.setEnabled(false);
        mRestoreButton.setEnabled(false);
        mLinkButton.setEnabled(false);
    }

    private void hideProgress() {
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        mStartButton.setEnabled(true);
        mBackupButton.setEnabled(true);
        mRestoreButton.setEnabled(true);
        mLinkButton.setEnabled(true);
    }
}
