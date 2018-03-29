package com.thevarunshah.simplebucketlist.internal;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;

public class FileBackupAgent extends BackupAgentHelper {

    private static final String FILENAME = "bucket_list.ser";
    private static final String FILES_BACKUP_KEY = "simple_bucket_list_files";

    @Override
    public void onCreate() {
        FileBackupHelper helper = new FileBackupHelper(this, FILENAME);
        addHelper(FILES_BACKUP_KEY, helper);
    }
}
