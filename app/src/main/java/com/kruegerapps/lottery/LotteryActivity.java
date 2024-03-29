package com.kruegerapps.lottery;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.commons.lang3.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main class
 *
 * @author skrueger
 */
public class LotteryActivity extends AppCompatActivity {
  /**
   * Whether or not the system UI should be auto-hidden after
   * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
   */
//  private static final boolean AUTO_HIDE = true;

  /**
   * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
   * user interaction before hiding the system UI.
   */
//  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

  /**
   * Some older devices needs a small delay between UI widget updates
   * and a change of the status and navigation bar.
   */
//  private static final int UI_ANIMATION_DELAY = 300;
  //    private final Handler mHideHandler = new Handler();
//  private View mContentView;
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
//  private View mControlsView;
//  private int mStatusCode;

  private final NumberPicker[] numberPickers = new NumberPicker[6];
  private boolean checked = false;
  private final Map<Integer, NumberPicker> pickerByNumber = new HashMap<>();
  //private RequestQueue requestQueue = null;

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
    requestURL();
//        new URLTask(this, checked).execute();

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
    MobileAds.initialize(this, initializationStatus -> {
      getSupportActionBar().setDisplayShowCustomEnabled(true);
      getSupportActionBar().setCustomView(getAdView());
    });
  }

  private AdView getAdView() {
    AdView mAdView = new AdView(this);
    mAdView.setAdSize(AdSize.BANNER);
    mAdView.setAdUnitId("ca-app-pub-8058982377649219/6931836093");
    AdRequest adRequest = new AdRequest.Builder().build();
    mAdView.loadAd(adRequest);
    return mAdView;
  }

  private void setupButton() {
    final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.shake);
    ImageButton myButton = findViewById(R.id.button1);
    myButton.setAnimation(myAnim);
    myButton.setOnClickListener(v -> {
      v.startAnimation(myAnim);
      // compare lucky numbers
      startLoadingAnimations();
      requestURL();
//            new URLTask(lotteryActivity, checked).execute();
    });
  }

  private void startLoadingAnimations() {
    startLoadingAnimation(findViewById(R.id.number1));
    startLoadingAnimation(findViewById(R.id.number2));
    startLoadingAnimation(findViewById(R.id.number3));
    startLoadingAnimation(findViewById(R.id.number4));
    startLoadingAnimation(findViewById(R.id.number5));
    startLoadingAnimation(findViewById(R.id.number6));
  }

  private void setupSwitcher() {
    Switch switcher = findViewById(R.id.switcher);
    switcher.setOnCheckedChangeListener((compoundButton, b) -> {
      ((TextView) findViewById(R.id.number1)).setText(StringUtils.EMPTY);
      ((TextView) findViewById(R.id.number2)).setText(StringUtils.EMPTY);
      ((TextView) findViewById(R.id.number3)).setText(StringUtils.EMPTY);
      ((TextView) findViewById(R.id.number4)).setText(StringUtils.EMPTY);
      ((TextView) findViewById(R.id.number5)).setText(StringUtils.EMPTY);
      ((TextView) findViewById(R.id.number6)).setText(StringUtils.EMPTY);
      checked = b;
      pickerByNumber.values()
                    .forEach(picker -> picker.setBackgroundColor(getResources().getColor(R.color.yellow, null)));
      startLoadingAnimations();
      requestURL();
//            new URLTask(lotteryActivity, checked).execute();
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

        //if (pickerByNumber.containsValue(picker)) {
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
//                                   .pickerByNumber(NumberPicker::getValue)
        //                                 .pickerByNumber(String::valueOf)
        //                               .collect(Collectors.joining(","));
      }
    });

    return np;
  }

//  private int getBallId(int tag) {
//    return tag == 1 ? R.id.number1 : tag == 2 ? R.id.number2 : tag == 3 ? R.id.number3 : tag == 4 ? R.id.number4 : tag == 5 ? R.id.number5 : R.id.number6;
//  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
//        delayedHide(100);
  }

  private void updateGui(List<Integer> winNumbers, String drawingDay) {
    if (!winNumbers.isEmpty()) {
      setNumberAndPicker(findViewById(R.id.number1), winNumbers.get(0));
      setNumberAndPicker(findViewById(R.id.number2), winNumbers.get(1));
      setNumberAndPicker(findViewById(R.id.number3), winNumbers.get(2));
      setNumberAndPicker(findViewById(R.id.number4), winNumbers.get(3));
      setNumberAndPicker(findViewById(R.id.number5), winNumbers.get(4));
      setNumberAndPicker(findViewById(R.id.number6), winNumbers.get(5));
    }
    ((TextView) findViewById(R.id.drawingDay)).setText(String.format("Ziehung vom %s", drawingDay));

    //winNumbers.stream().pickerByNumber(pickerByNumber::get).filter(Objects::nonNull);
    // winNumbers.forEach(num -> {
    //   NumberPicker numberPicker = pickerByNumber.get(num);
    //  if (numberPicker != null) {
    //    ((TextView) findViewById(R.id.number1)).setBackgroundColor(Color.GREEN);
    //  }
    // });
  }

  private void setNumberAndPicker(TextView ball, Integer num) {
    ball.setText(String.valueOf(num));
    ball.clearAnimation();


    SpringAnimation anim = new SpringAnimation(ball, DynamicAnimation.ROTATION_Y, 0);
    anim.setStartValue(-35);
    anim.setStartVelocity(2000);
    anim.getSpring()
        .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
    anim.getSpring()
        .setStiffness(SpringForce.STIFFNESS_LOW);
    anim.start();

    setNumberAndPickerColor(num);
  }

  private void setNumberAndPickerColor(Integer num) {
    NumberPicker numberPicker = pickerByNumber.get(num);
    if (numberPicker != null) {
      //ball.getBackground()
      //    .setTint(Color.GREEN);
      numberPicker.setBackgroundColor(Color.GREEN);
    }
  }

  private void startLoadingAnimation(TextView tv) {
    Animation anim = AnimationUtils.loadAnimation(getApplicationContext(),
            R.anim.rotate);
    anim.setRepeatCount(Animation.INFINITE);
    tv.startAnimation(anim);
    tv.setText(StringUtils.EMPTY);
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

  private void requestURL() {
    pickerByNumber.clear();
    //final long l = System.currentTimeMillis();
    Ion.with(getApplicationContext())
       .load(String.format(getResources().getString(R.string.lottery_url), checked ? "0" : "1"))
       .asJsonObject()
       .setCallback((e, result) -> {
         if (result != null) {
           //System.out.println("XXX getNumbersFromBackupServer1: " + (System.currentTimeMillis() - l));
           getAndShowNumbers(result);
         }
         else {
           if (e != null) {
             e.printStackTrace();
           }
           getAndShowNumbersFromBackupServer();
         }
       });
    //System.out.println("XXX getAndShowNumbersFromBackupServer: " + (System.currentTimeMillis() - l));
  }

  private void getAndShowNumbers(JsonObject json) {
    List<Integer> winNumbers = new ArrayList<>();
    String drawingDay;
    Boolean error = Boolean.FALSE;
    try {
      json.get("numbers")
          .getAsJsonArray()
          .forEach(elem -> winNumbers.add(processNumber(elem.getAsInt())));

      if (winNumbers.size() == 6) {
        showNumbers(winNumbers, json.get("drawingDay")
                                    .getAsString());
      }
      else {
        error = Boolean.TRUE;
      }
    } catch (Exception e) {
      error = Boolean.TRUE;
    }

    //long l = System.currentTimeMillis();
    if (error) {
      getAndShowNumbersFromBackupServer();
    }
    //System.out.println("XXX getAndShowNumbersFromBackupServer: " + (System.currentTimeMillis() - l));
  }

  private void showNumbers(List<Integer> winNumbers, String drawingDay) {
    if (!winNumbers.isEmpty() && drawingDay != null) {
      updateGui(winNumbers, drawingDay);
      int duration = Toast.LENGTH_SHORT;
      Toast toast = Toast.makeText(getApplicationContext(), String.format("Sie haben %s richtige Zahl%s", pickerByNumber.keySet()
                                                                                                                        .size(), pickerByNumber.keySet()
                                                                                                                                               .size() == 1 ? StringUtils.EMPTY : "en"), duration);
      toast.show();
    }
  }

  private void getAndShowNumbersFromBackupServer() {
    //final long l = System.currentTimeMillis();
    pickerByNumber.clear();

    Ion.with(getApplicationContext())
       .load(getResources().getString(R.string.lottery_url_backup))
       .asString()
       .setCallback((e, result) -> {
         if (result != null && e == null) {
           //System.out.println("XXX getNumbersFromBackupServer2: " + (System.currentTimeMillis() - l));
           AbstractMap.SimpleEntry<List<Integer>, String> resultBackup = getNumbers(result);
           showNumbers(resultBackup.getKey(), resultBackup.getValue());
         }
         else {
           e.printStackTrace();
         }
       });
  }

  private AbstractMap.SimpleEntry<List<Integer>, String> getNumbers(String buffer) {
    List<Integer> winNumbers = new ArrayList<>();
    String drawingDay = null;
    List<String> numbers = Collections.emptyList();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      LocalDate now = LocalDate.now();
      if (checked && now.getDayOfWeek()
                        .equals(DayOfWeek.SATURDAY) || !checked && now.getDayOfWeek()
                                                                      .equals(DayOfWeek.WEDNESDAY)) {
        drawingDay = String.format("%s.%s.%s", String.format("%02d", now.getDayOfMonth()), String.format("%02d", now.getMonthValue()), now.getYear());
        numbers = getNumbers(buffer, drawingDay);
      }

      if (numbers == null || numbers.isEmpty()) {
        // not yet drawn today or not drawing day
        LocalDate then = now.with(TemporalAdjusters.previous(checked ? DayOfWeek.SATURDAY : DayOfWeek.WEDNESDAY));
        drawingDay = String.format("%s.%s.%s", String.format("%02d", then.getDayOfMonth()), String.format("%02d", then.getMonthValue()), then.getYear());
        numbers = getNumbers(buffer, drawingDay);
      }
    }
    else {
      AbstractMap.SimpleEntry<List<String>, String> numbersOldWay = getNumbersLegacy(buffer, numbers);
      numbers = numbersOldWay.getKey();
      drawingDay = numbersOldWay.getValue();
    }

    if (numbers != null) {
      for (int i = 0; i < numbers.size(); i++) {
        if (i > 0 && i < 7) {
          winNumbers.add(processNumber(Integer.valueOf(numbers.get(i))));
        }
      }
    }

    return new AbstractMap.SimpleEntry<>(winNumbers, drawingDay);
  }

  private List<String> getNumbers(String buffer, String drawingDay) {
    int ind = buffer.indexOf(drawingDay);
    if (ind > -1) {
      return Stream.of(buffer.substring(ind)
                             .split("LottoBall__circle\">"))
                   .map(s -> s.substring(0, s.indexOf("<")))
                   .collect(Collectors.toList());
    }
    return null;
  }

  private AbstractMap.SimpleEntry<List<String>, String> getNumbersLegacy(String buffer, List<String> numbers) {
    String drawingDay = null;
    Calendar cal = Calendar.getInstance();
    int day = cal.get(Calendar.DAY_OF_WEEK);
    if (day == Calendar.SATURDAY || day == Calendar.WEDNESDAY) {
      drawingDay = String.format("%s.%s.%s", String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)), String.format("%02d", cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR));
      numbers = getNumbers(buffer, drawingDay);
    }

    if (numbers == null || numbers.isEmpty()) {
      int amount;
      if (!checked) {
        amount = day > Calendar.WEDNESDAY ? day - Calendar.WEDNESDAY : day + 3;
      }
      else {
        amount = day;
      }

      cal.add(Calendar.DAY_OF_WEEK, -amount);
      drawingDay = String.format("%s.%s.%s", String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)), String.format("%02d", cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR));
      numbers = getNumbers(buffer, drawingDay);
    }
    return new AbstractMap.SimpleEntry<>(numbers, drawingDay);
  }

  private Integer processNumber(Integer number) {
    Optional<NumberPicker> first = Stream.of(numberPickers)
                                         .filter(np -> number == np.getValue())
                                         .findFirst();
    first.ifPresent(numberPicker -> pickerByNumber.put(number, numberPicker));
    return number;
  }

//
//    private class URLTask extends AsyncTask<Void, Void, Void> {
//
//        protected AsyncResponse delegate;
//        private final boolean checked;
//
//        private URLTask(LotteryActivity fullscreenActivity, boolean checked) {
//            this.delegate = fullscreenActivity;
//            this.checked = checked;
//        }
//
//        protected void onPreExecute() {
//            pickerByNumber.clear();
//            winNumbers.clear();
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            delegate.updateGui(winNumbers, drawingDay);
//
//            int duration = Toast.LENGTH_SHORT;
//            Toast toast = Toast.makeText(getApplicationContext(), String.format("Sie haben %s richtige Zahl%s", pickerByNumber.keySet().size(), pickerByNumber.keySet().size() == 1 ? StringUtils.EMPTY : "en"), duration);
//            toast.show();
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
////            long t = System.currentTimeMillis();
//            URL urlObject;
//            Boolean tryAgain = Boolean.FALSE;
//            try {
//                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new URL(String.format(getResources().getString(R.string.lottery_url), checked ? "0" : "1")).openConnection()
//                                                                                                                                                                                 .getInputStream()))) {
//                    String json = buffer.lines()
//                                        .collect(Collectors.joining());
//                    JSONObject j = new JSONObject(json);
//                    drawingDay = j.getString("drawingDay");
//                    JSONArray numbers = j.getJSONArray("numbers");
//                    for (int i = 0; i < numbers.length(); i++) {
//                        winNumbers.add(processNumber((Integer) numbers.get(i)));
//                    }
//                    if (winNumbers.size() != 6) {
//                        tryAgain = Boolean.TRUE;
//                    }
//                } catch (Exception e) {
//                    tryAgain = Boolean.TRUE;
//                }
//
//                if (tryAgain) {
//                    urlObject = new URL(getResources().getString(R.string.lottery_url_backup));
//                    URLConnection urlConnection = urlObject.openConnection();
//                    try (BufferedReader buffer = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
//                        List<String> numbers = Collections.emptyList();
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            LocalDate now = LocalDate.now();
//                            if (checked && now.getDayOfWeek()
//                                              .equals(DayOfWeek.SATURDAY) || !checked && now.getDayOfWeek()
//                                                                                            .equals(DayOfWeek.WEDNESDAY)) {
//                                numbers = getNumbers(new BufferedReader(new InputStreamReader(new URL(getResources().getString(R.string.lottery_url)).openConnection()
//                                                                                                                                                     .getInputStream())), String.format("%02d", now.getDayOfMonth()), String.format("%02d", now.getMonthValue()), now.getYear());
//                            }
//
//                            if (numbers.isEmpty()) {
//                                // not yet drawn today or not drawing day
//                                LocalDate then = now.with(TemporalAdjusters.previous(checked ? DayOfWeek.SATURDAY : DayOfWeek.WEDNESDAY));
//                                numbers = getNumbers(buffer, String.format("%02d", then.getDayOfMonth()), String.format("%02d", then.getMonthValue()), then.getYear());
//                            }
//                        } else {
//                            numbers = getNumbersOldWay(buffer, numbers);
//                        }
//
//                        for (int i = 0; i < numbers.size(); i++) {
//                            if (i > 0 && i < 7) {
//                                winNumbers.add(processNumber(Integer.valueOf(numbers.get(i))));
//                            }
//                        }
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
////            System.out.println(System.currentTimeMillis() - t);
//
//            return null;
//        }
//
//        private List<String> getNumbersOldWay(BufferedReader buffer, List<String> numbers) {
//            Calendar cal = Calendar.getInstance();
//            int day = cal.get(Calendar.DAY_OF_WEEK);
//            if (day == Calendar.SATURDAY || day == Calendar.WEDNESDAY) {
//                numbers = getNumbers(buffer, String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)), String.format("%02d", cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR));
//            }
//
//            if (numbers.isEmpty()) {
//                int amount = 0;
//                if (!checked) {
//                    amount = day > Calendar.WEDNESDAY ? day - Calendar.WEDNESDAY : day + 3;
//                } else {
//                    amount = day;
//                }
//
//                cal.add(Calendar.DAY_OF_WEEK, -amount);
//                numbers = getNumbers(buffer, String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)), String.format("%02d", cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR));
//            }
//            return numbers;
//        }
//
//        private Integer processNumber(Integer number) {
//            Optional<NumberPicker> first = Stream.of(numberPickers)
//                                                 .filter(np -> number == np.getValue())
//                                                 .findFirst();
//            if (first.isPresent()) {
//                pickerByNumber.put(number, first.get());
//            }
//            return number;
//        }
//
//        private List<String> getNumbers(BufferedReader buffer, String dayOfMonth, String monthValue, int year) {
//            drawingDay = String.format("%s.%s.%s", dayOfMonth, monthValue, year);
//            return buffer.lines()
//                         .pickerByNumber(str -> {
//                             int ind = str.indexOf(drawingDay);
//                             return ind > -1 ? str.substring(ind) : null;
//                         })
//                         .filter(Objects::nonNull)
//                         .pickerByNumber(str -> Stream.of(str.split("LottoBall__circle\">"))
//                                           .pickerByNumber(s -> s.substring(0, s.indexOf("<")))
//                                           .collect(Collectors.toList()))
//                         .flatMap(List::stream)
//                         .collect(Collectors.toList());
//        }
//    }

}
