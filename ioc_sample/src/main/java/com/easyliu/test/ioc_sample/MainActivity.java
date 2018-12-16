package com.easyliu.test.ioc_sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.easyliu.test.annotation.BindView;
import com.easyliu.test.ioc.ViewInjector;

public class MainActivity extends AppCompatActivity {

  @BindView(R.id.tv_test) TextView mTextView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ViewInjector.injectView(this);
    mTextView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        Toast.makeText(MainActivity.this, "show Test!", Toast.LENGTH_LONG);
      }
    });
  }
}
