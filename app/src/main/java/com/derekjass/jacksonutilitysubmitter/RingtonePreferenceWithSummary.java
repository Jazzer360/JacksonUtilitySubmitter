package com.derekjass.jacksonutilitysubmitter;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.util.AttributeSet;

public class RingtonePreferenceWithSummary extends RingtonePreference {
    public RingtonePreferenceWithSummary(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValueObj) {
        super.onSetInitialValue(restorePersistedValue, defaultValueObj);
        updateSummary();
    }

    @Override
    protected void onSaveRingtone(Uri ringtoneUri) {
        super.onSaveRingtone(ringtoneUri);
        updateSummary();
    }

    private void updateSummary() {
        Context ctx = getContext();
        String uriString = getPersistedString("content://settings/system/notification_sound");
        if (uriString.length() == 0) {
            setSummary(R.string.none);
        } else {
            Ringtone ringtone = RingtoneManager.getRingtone(ctx, Uri.parse(uriString));
            setSummary(ringtone.getTitle(ctx));
        }
    }
}
