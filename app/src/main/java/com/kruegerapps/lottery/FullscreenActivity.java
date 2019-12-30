package com.kruegerapps.lottery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
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

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements AsyncResponse {
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

  private final NumberPicker[] numberPickers = new NumberPicker[6];
  private final List<Integer> winNumbers = new ArrayList<>();
  private boolean checked = false;
  private String drawingDay;
  private final Map<Integer, NumberPicker> map = new HashMap<>();

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
    setupAdvertise();
    // switch between drawing days
    setupSwitcher();
    // switch between different sets
    setupSeekBar();
    // set wheels with lucky numbers
    setupPickers();
    // set action & jiggling of button if pressed
    setupButton();
    // execute initially
    new URLTask(this, checked).execute();

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

  private void setupPickers() {
    numberPickers[0] = createPicker(R.id.np1, 1);
    numberPickers[1] = createPicker(R.id.np2, 2);
    numberPickers[2] = createPicker(R.id.np3, 3);
    numberPickers[3] = createPicker(R.id.np4, 4);
    numberPickers[4] = createPicker(R.id.np5, 5);
    numberPickers[5] = createPicker(R.id.np6, 6);
  }

  private void setupAdvertise() {
    MobileAds.initialize(this, initializationStatus -> {});
    getSupportActionBar().setDisplayShowCustomEnabled(true);
    getSupportActionBar().setCustomView(getAdView());
  }

  private AdView getAdView() {
    AdView mAdView = new AdView(this);
    mAdView.setAdSize(AdSize.SMART_BANNER);
    mAdView.setAdUnitId("ca-app-pub-8058982377649219/6931836093");
    AdRequest adRequest = new AdRequest.Builder().build();
    mAdView.loadAd(adRequest);
    return mAdView;
  }

  private void setupButton() {
    final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.shake);
    ImageButton myButton = findViewById(R.id.button1);
    myButton.setAnimation(myAnim);
    FullscreenActivity fullscreenActivity = this;
    myButton.setOnClickListener(v -> {
      v.startAnimation(myAnim);
      // compare lucky numbers
      new URLTask(fullscreenActivity, checked).execute();
    });
  }

  private void setupSwitcher() {
    Switch switcher = findViewById(R.id.switcher);
    final FullscreenActivity fullscreenActivity = this;
    switcher.setOnCheckedChangeListener((compoundButton, b) -> {
      ((TextView)findViewById(R.id.number1)).setText(StringUtils.EMPTY);
      ((TextView)findViewById(R.id.number2)).setText(StringUtils.EMPTY);
      ((TextView)findViewById(R.id.number3)).setText(StringUtils.EMPTY);
      ((TextView)findViewById(R.id.number4)).setText(StringUtils.EMPTY);
      ((TextView)findViewById(R.id.number5)).setText(StringUtils.EMPTY);
      ((TextView)findViewById(R.id.number6)).setText(StringUtils.EMPTY);
      checked = b;
      map.values()
         .forEach(picker -> picker.setBackgroundColor(getResources().getColor(R.color.yellow, null)));
      new URLTask(fullscreenActivity, checked).execute();
    });
    switcher.setTextColor(getResources().getColor(R.color.yellow, null));
    switcher.setTextOff(getResources().getString(R.string.wed));
    switcher.setTextOn(getResources().getString(R.string.sat));
  }

  private void setupSeekBar() {
    SeekBar seekBar = findViewById(R.id.seekBar);
    seekBar.setMax(9);
    seekBar.incrementProgressBy(1);
    seekBar.setProgress(0);

    final TextView barNumber = findViewById(R.id.barNumber);
    barNumber.setText(String.valueOf(seekBar.getProgress() + 1));

    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //System.out.println("YYYYY");
        barNumber.setText(String.valueOf(progress + 1));
        AtomicInteger i = new AtomicInteger(0);
        Stream.of(numberPickers)
              .forEach(np -> {
                i.incrementAndGet();
                int val = getPreferences(Context.MODE_PRIVATE).getInt(getString(R.string.pref_key) + barNumber.getText() + i.get(), i.get());
                np.setValue(val);
              });

        int color = getResources().getColor(R.color.yellow, null);
        Stream.of(numberPickers)
           .forEach(np -> {
             //((TextView) findViewById(getBallId((Integer) entry.getValue().getTag()))).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle));
             np.setBackgroundColor(color);
           });
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
  }

  private NumberPicker createPicker(int id, int number) {
    NumberPicker np = findViewById(id);
    np.setTag(number);
    np.setMinValue(1);
    np.setMaxValue(49);
    np.setWrapSelectorWheel(true);
    CharSequence block = ((TextView) findViewById(R.id.barNumber)).getText();
    if (block != null) {
      int val = getPreferences(Context.MODE_PRIVATE).getInt(getString(R.string.pref_key) + block + number, number);
      np.setValue(val);
    }

    np.setOnValueChangedListener((picker, oldVal, newVal) -> {
      CharSequence block1 = ((TextView) findViewById(R.id.barNumber)).getText();
      if (block1 != null) {
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        int tag = (int) picker.getTag();
        editor.putInt(getString(R.string.pref_key) + block1 + tag, newVal);
        editor.apply();

        //if (map.containsValue(picker)) {
          picker.setBackgroundColor(getResources().getColor(R.color.yellow, null));
        //}
        //    if (string.isEmpty()) {
        //    int i=0;
        //  String numbers = StringUtils.EMPTY;
        //for (NumberPicker numberPicker : numberPickers) {
        //if (i == (int)picker.getTag()){
        // numbers +=
        //}
        //}
        //String numbers = Stream.of(numberPickers[Integer.valueOf(block.toString())])
//                                   .map(NumberPicker::getValue)
        //                                 .map(String::valueOf)
        //                               .collect(Collectors.joining(","));
      }
    });

    return np;
  }

  private int getBallId(int tag) {
    return tag == 1 ? R.id.number1 : tag == 2 ? R.id.number2 : tag == 3 ? R.id.number3 : tag == 4 ? R.id.number4 : tag == 5 ? R.id.number5 : R.id.number6;
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
//        delayedHide(100);
  }

  @Override
  public void processFinish() {
    if (!winNumbers.isEmpty()) {
      setBallAndPicker(findViewById(R.id.number1), winNumbers.get(0));
      setBallAndPicker(findViewById(R.id.number2), winNumbers.get(1));
      setBallAndPicker(findViewById(R.id.number3), winNumbers.get(2));
      setBallAndPicker(findViewById(R.id.number4), winNumbers.get(3));
      setBallAndPicker(findViewById(R.id.number5), winNumbers.get(4));
      setBallAndPicker(findViewById(R.id.number6), winNumbers.get(5));
    }
    ((TextView) findViewById(R.id.drawingDay)).setText(String.format("Ziehung vom %s", drawingDay));

    //winNumbers.stream().map(map::get).filter(Objects::nonNull);
    // winNumbers.forEach(num -> {
    //   NumberPicker numberPicker = map.get(num);
    //  if (numberPicker != null) {
    //    ((TextView) findViewById(R.id.number1)).setBackgroundColor(Color.GREEN);
    //  }
    // });
  }

  private void setBallAndPicker(TextView ball, Integer num) {
    setView(ball, String.valueOf(num));
    setBallAndPickerColor(ball, num);
  }

  private void setBallAndPickerColor(TextView ball, Integer num) {
    NumberPicker numberPicker = map.get(num);
    if (numberPicker != null) {
      //ball.getBackground()
      //    .setTint(Color.GREEN);
      numberPicker.setBackgroundColor(Color.GREEN);
    }
  }

  private void setView(TextView tv, String value) {
    SpringAnimation anim = new SpringAnimation(tv, DynamicAnimation.ROTATION_Y, 0);
    anim.setStartValue(-35);
    anim.setStartVelocity(2000);
    anim.getSpring()
        .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
    anim.getSpring()
        .setStiffness(SpringForce.STIFFNESS_LOW);
    anim.start();
    tv.setText(value);
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

  private class URLTask extends AsyncTask<Void, Void, Void> {

    protected AsyncResponse delegate;
    private final boolean checked;
    private int hits = 0;

    private URLTask(FullscreenActivity fullscreenActivity, boolean checked) {
      this.delegate = fullscreenActivity;
      this.checked = checked;
    }

    protected void onPreExecute() {
      map.clear();
      winNumbers.clear();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      delegate.processFinish();

      int duration = Toast.LENGTH_SHORT;
      Toast toast = Toast.makeText(getApplicationContext(), String.format("Sie haben %s richtige Zahl%s", hits, hits == 1 ? StringUtils.EMPTY : "en"), duration);
      toast.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {

      URL urlObject;
      try {
        urlObject = new URL(getResources().getString(R.string.lottery_url));
        URLConnection urlConnection = urlObject.openConnection();

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
          List<String> numbers = Collections.emptyList();
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate now = LocalDate.now();
            if (checked && now.getDayOfWeek()
                   .equals(DayOfWeek.SATURDAY) || !checked && now.getDayOfWeek()
                                                     .equals(DayOfWeek.WEDNESDAY)) {
              numbers = getNumbers(new BufferedReader(new InputStreamReader(new URL(getResources().getString(R.string.lottery_url)).openConnection().getInputStream())), now.getDayOfMonth(), now.getMonthValue(), now.getYear());
            }

            if (numbers.isEmpty()) {
              // not yet drawn today or not drawing day
              LocalDate then = now.with(TemporalAdjusters.previous(checked ? DayOfWeek.SATURDAY : DayOfWeek.WEDNESDAY));
              numbers = getNumbers(buffer, then.getDayOfMonth(), then.getMonthValue(), then.getYear());
            }
          } else {
            Calendar cal = Calendar.getInstance();
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
              numbers = getNumbers(buffer, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
            }

            if (numbers.isEmpty()) {
              cal.add(Calendar.DAY_OF_WEEK, -(cal.get(Calendar.DAY_OF_WEEK)) - (checked ? 0 : 3));
              numbers = getNumbers(buffer, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
            }
          }

          for (int i = 0; i < numbers.size(); i++) {
            if (i > 0 && i < 7) {
              Integer number = Integer.valueOf(numbers.get(i));
              Optional<NumberPicker> first = Stream.of(numberPickers)
                                                   .filter(np -> number == np.getValue())
                                                   .findFirst();
              if (first.isPresent()) {
                hits++;
                map.put(number, first.get());
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

    private List<String> getNumbers(BufferedReader buffer, int dayOfMonth, int monthValue, int year) {
      drawingDay = String.format("%s.%s.%s", dayOfMonth, monthValue, year);
      return buffer.lines()
                   .map(str -> {
                     int ind = str.indexOf(drawingDay);
                     return ind > -1 ? str.substring(ind) : null;
                   })
                   .filter(Objects::nonNull)
                   .map(str -> Stream.of(str.split("LottoBall__circle\">"))
                                     .map(s -> s.substring(0, s.indexOf("<")))
                                     .collect(Collectors.toList()))
                   .flatMap(List::stream)
                   .collect(Collectors.toList());
    }
  }

}
