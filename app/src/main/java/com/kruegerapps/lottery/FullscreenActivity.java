package com.kruegerapps.lottery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
  /**
   * Whether or not the system UI should be auto-hidden after
   * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
   */
  private static final boolean AUTO_HIDE = true;

  /**
   * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
   * user interaction before hiding the system UI.
   */
  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

  /**
   * Some older devices needs a small delay between UI widget updates
   * and a change of the status and navigation bar.
   */
  private static final int UI_ANIMATION_DELAY = 300;
  //    private final Handler mHideHandler = new Handler();
  private View mContentView;
  //    private final Runnable mHidePart2Runnable = new Runnable() {
//        @SuppressLint("InlinedApi")
//        @Override
//        public void run() {
//            // Delayed removal of status and navigation bar
//
//            // Note that some of these constants are new as of API 16 (Jelly Bean)
//            // and API 19 (KitKat). It is safe to use them, as they are inlined
//            // at compile-time and do nothing on earlier devices.
//            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        }
//    };
  private View mControlsView;

  private List<String> numbers = Collections.emptyList();
  private NumberPicker[] numberPickers = new NumberPicker[6];
  List<Integer> winNumbers = new ArrayList<>();

//    private final Runnable mShowPart2Runnable = new Runnable() {
//        @Override
//        public void run() {
//            // Delayed display of UI elements
//            ActionBar actionBar = getSupportActionBar();
//            if (actionBar != null) {
//                actionBar.show();
//            }
//            mControlsView.setVisibility(View.VISIBLE);
//        }
//    };
//    private boolean mVisible;
//    private final Runnable mHideRunnable = new Runnable() {
//        @Override
//        public void run() {
//            hide();
//        }
//    };

  /**
   * Touch listener to use for in-layout UI controls to delay hiding the
   * system UI. This is to prevent the jarring behavior of controls going away
   * while interacting with activity UI.
   */
//    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            if (AUTO_HIDE) {
//                delayedHide(AUTO_HIDE_DELAY_MILLIS);
//            }
//            return false;
//        }
//    };
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_fullscreen);
    // setup advertise
    MobileAds.initialize(this, new OnInitializationCompleteListener() {
      @Override
      public void onInitializationComplete(InitializationStatus initializationStatus) {
      }
    });
    getSupportActionBar().setDisplayShowCustomEnabled(true);
    getSupportActionBar().setCustomView(getAdView());
    // switch between drawing days
    setupSwitcher();
    // switch between different sets
    setupSeekBar();
    // set wheels with lucky numbers
    numberPickers[0] = createPicker(R.id.np1);
    numberPickers[1] = createPicker(R.id.np2);
    numberPickers[2] = createPicker(R.id.np3);
    numberPickers[3] = createPicker(R.id.np4);
    numberPickers[4] = createPicker(R.id.np5);
    numberPickers[5] = createPicker(R.id.np6);
    // set action & jiggling of button if pressed
    setupButton();

//        mVisible = true;
//        mControlsView = findViewById(R.id.fullscreen_content_controls);
//        mContentView = findViewById(R.id.fullscreen_content);
    // Set up the user interaction to manually show or hide the system UI.
//        mContentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });
    // Upon interacting with UI controls, delay any scheduled hide()
    // operations to prevent the jarring behavior of controls going away
    // while interacting with the UI.
//        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
  }

  private void setupButton() {
    final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.shake);
    ImageButton myButton = (ImageButton) findViewById(R.id.button1);
    myButton.setAnimation(myAnim);
    myButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        v.startAnimation(myAnim);

        new URLTask().execute();

        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXX");
      }
    });
  }

  private void setupSwitcher() {
    Switch switcher = (Switch) findViewById(R.id.switcher);
    switcher.setTextColor(getResources().getColor(R.color.yellowFromTheEgg, null));
    switcher.setTextOff(getResources().getString(R.string.wed));
    switcher.setTextOn(getResources().getString(R.string.sat));
  }

  private void setupSeekBar() {
    SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
    seekBar.setMax(9);
    seekBar.incrementProgressBy(1);
    seekBar.setProgress(0);

    final TextView barNumber = (TextView) findViewById(R.id.barNumber);
    barNumber.setText(String.valueOf(seekBar.getProgress() + 1));

    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        barNumber.setText(String.valueOf(progress + 1));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
  }

  private AdView getAdView() {
    AdView mAdView = new AdView(this);
    mAdView.setAdSize(AdSize.SMART_BANNER);
    mAdView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
    AdRequest adRequest = new AdRequest.Builder().build();
    mAdView.loadAd(adRequest);
    return mAdView;
  }

  private NumberPicker createPicker(int id) {
    NumberPicker np = (NumberPicker) findViewById(id);
    np.setMinValue(0);
    np.setMaxValue(49);
    np.setWrapSelectorWheel(true);
    np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
      @Override
      public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        //Display the newly selected number from picker
//                tv.setText("Selected Number : " + newVal);
      }
    });

    return np;
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
//        delayedHide(100);
  }
//
//    private void toggle() {
//        if (mVisible) {
//            hide();
//        } else {
//            show();
//        }
//    }

//    private void hide() {
//        // Hide UI first
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.hide();
//        }
//        mControlsView.setVisibility(View.GONE);
//        mVisible = false;
//
//        // Schedule a runnable to remove the status and navigation bar after a delay
//        mHideHandler.removeCallbacks(mShowPart2Runnable);
//        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
//    }

//    @SuppressLint("InlinedApi")
//    private void show() {
//        // Show the system bar
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//        mVisible = true;
//
//        // Schedule a runnable to display UI elements after a delay
//        mHideHandler.removeCallbacks(mHidePart2Runnable);
//        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
//    }
//
//    /**
//     * Schedules a call to hide() in delay milliseconds, canceling any
//     * previously scheduled calls.
//     */
//    private void delayedHide(int delayMillis) {
//        mHideHandler.removeCallbacks(mHideRunnable);
//        mHideHandler.postDelayed(mHideRunnable, delayMillis);
//    }

  class URLTask extends AsyncTask<Void, Void, Void> {

    int hits = 0;

    protected void onPreExecute() {
      //display progress dialog.

    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);


      Context context = getApplicationContext();
      int duration = Toast.LENGTH_SHORT;
      Toast toast = Toast.makeText(context, String.format("Sie haben %s richtige Zahlen", hits), duration);
      toast.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {

      URL urlObject = null;
      try {
//          RequestQueue queue = Volley.newRequestQueue(this);
//          String url ="http://www.google.com";
//
//// Request a string response from the provided URL.
//          StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                  new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                      // Display the first 500 characters of the response string.
//                      textView.setText("Response is: "+ response.substring(0,500));
//                    }
//                  }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//              textView.setText("That didn't work!");
//            }
//          });
//
//// Add the request to the RequestQueue.
//          queue.add(stringRequest);



        urlObject = new URL("https://www.lotto.de/lotto-6aus49");
        URLConnection urlConnection = urlObject.openConnection();

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
          winNumbers.clear();

          numbers = buffer.lines().map(str -> {
            int ind = str.indexOf("18.12.2019");
            return ind > -1 ? str.substring(ind, str.length()) : null;
          }).filter(Objects::nonNull).map(str -> Stream.of(str.split("LottoBall__circle\\\">")).map(s -> s.substring(0, s.indexOf("<"))).collect(Collectors.toList())).flatMap(List::stream).collect(Collectors.toList());
          for (int i = 0; i < numbers.size(); i++) {
            if (i > 0 && i < 7) {
              Integer number = Integer.valueOf(numbers.get(i));
              if (Stream.of(numberPickers).map(NumberPicker::getValue).filter(number::equals).findFirst().isPresent()) {
                hits++;
              }
              winNumbers.add(number);
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }


      return null;
    }

  }

}
