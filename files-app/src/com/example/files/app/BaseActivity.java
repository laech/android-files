package com.example.files.app;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import com.example.files.R;

public class BaseActivity extends FragmentActivity {

  @Override public void startActivityForResult(Intent intent, int requestCode) {
    super.startActivityForResult(intent, requestCode);
    overrideStartActivityAnimation();
  }

  @Override public void startActivity(Intent intent) {
    super.startActivity(intent);
    overrideStartActivityAnimation();
  }

  @Override public void finish() {
    super.finish();
    overrideFinishActivityAnimation();
  }

  private void overrideStartActivityAnimation() {
    overridePendingTransition(R.anim.activity_appear, R.anim.still);
  }

  private void overrideFinishActivityAnimation() {
    overridePendingTransition(R.anim.still, R.anim.activity_disappear);
  }
}
