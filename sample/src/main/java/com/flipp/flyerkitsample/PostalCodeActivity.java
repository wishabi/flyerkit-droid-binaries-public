package com.flipp.flyerkitsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class PostalCodeActivity extends AppCompatActivity {
    private FlyerKitApplication mgv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mgv = (FlyerKitApplication) getApplicationContext();
        setContentView(R.layout.activity_postal_code);

        // postal code input
        final EditText editText = (EditText) findViewById(R.id.postal_code);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // launch store select screen on keyboard done
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String postalCode = editText.getText().toString();
                    mgv.setPostalCode(postalCode);
                    Intent intent = new Intent(PostalCodeActivity.this, StoreListingActivity.class);
                    startActivity(intent);
                }
                return true;
            }
        });

        setTitle("Enter Zip/Postal Code");
    }

    // method called when clicking on the default flyer button
    public void gotoDefaultFlyer(View v) {
        Intent intent = new Intent(PostalCodeActivity.this, FlyerActivity.class);
        intent.putExtra("flyerId", mgv.defaultFlyerId);
        intent.putExtra("postalCode", mgv.postalCode);
        intent.putExtra("accessToken", mgv.accessToken);
        startActivity(intent);
    }
}
