package alarm.alarm;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // Minimum şifre uzunluğu
    private static int MIN_PASSWORD_LEN = 4;
    // Alarm iptal şifremiz
    private static String PASSWORD = "HasanUgurYazgan";

    private Timer timer = null;
    // Alarm kurulu mu değil mi? (Alarm durumu)
    private boolean alarmActive = false;
    // Kurulan alarm saati
    private int alarmHour;
    // Kurulan alarm dakikası
    private int alarmMinute;
    // Alarm çalıyor mu?
    private boolean alarmRunning = false;
    // Sound Stream ID
    private int streamId = -1;
    // Sound Pool
    private SoundPool soundPool = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Alarm için zaman seçimi
        final TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        // Alarm başlatma butonu
        final Button buttonStart = (Button) findViewById(R.id.buttonStart);
        // Alarm iptal butonu
        final Button buttonStop = (Button) findViewById(R.id.buttonStop);
        // Şifre atama butonu
        final Button buttonPassword = (Button) findViewById(R.id.buttonPassword);

        // Alarm başlatma butonu için tıklanma olayı üretiyoruz
        buttonStart.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Alarm aktif ise alarmı yeniden kurmayacağız, önce durdurmak gerekecek
                if(!alarmActive) {
                    // Şu anki zamanı alıyoruz
                    Calendar c = Calendar.getInstance();
                    int currentHour = c.get(Calendar.HOUR_OF_DAY);
                    int currentMinute = c.get(Calendar.MINUTE);
                    int currentSecond = c.get(Calendar.SECOND);

                    // Alarm için verilen zamanı alıyoruz
                    timePicker.clearFocus();
                    alarmHour = timePicker.getCurrentHour();
                    alarmMinute = timePicker.getCurrentMinute();

                    // Alarm için verilen zamandan şu anki zamanı çıkarıp alarmın kaç saniye sonra çalacağını buluyoruz
                    int diff = (alarmHour * 60 + alarmMinute) * 60 - ((currentHour * 60 + currentMinute) * 60 + currentSecond);

                    // Bu süre negatif ise alarm için seçilen zaman şu anki zamandan öncedir bu durumda bu süreye bir gün ekliyoruz
                    if (diff < 0) {
                        diff += 24 * 60 * 60;
                    }

                    // Alarm durumunu aktif ediyoruz, zamanlayıcımızı kuruyoruz
                    alarmActive = true;
                    timer = new Timer();
                    timer.schedule(new AlarmTimerTask(), diff * 1000L);

                    // Alarm için kalan süreyi yazı olarak bastırmak için saat, dakika ve saniye cinsinden bu süreyi hesaplıyoruz
                    int h = diff / (60 * 60);
                    int m = diff / 60 - h * 60;
                    int s = diff % 60;

                    String infoText = "";

                    if(h > 0) {
                        infoText += h + " saat";

                        if(m > 0) {
                            infoText += " " + m + " dakika";

                            if(s > 0) {
                                infoText += " " + s + " saniye";
                            }
                        }
                    } else {
                        if(m > 0) {
                            infoText += m + " dakika";

                            if(s > 0) {
                                infoText += " " + s + " saniye";
                            }
                        } else {
                            infoText += s + " saniye";
                        }
                    }

                    // Alarm kuruldu ve zaman bilgisini ekrana bildirim olarak basıyoruz
                    Toast.makeText(MainActivity.this, "Alarm kuruldu (" + infoText + ")", Toast.LENGTH_SHORT).show();
                } else {
                    // Burada Alarm zaten kurulduğu için sadece alarm bilgilerini bildirim olarak basacağız
                    // Şu anki zamanı alıyoruz
                    Calendar c = Calendar.getInstance();
                    int currentHour = c.get(Calendar.HOUR_OF_DAY);
                    int currentMinute = c.get(Calendar.MINUTE);
                    int currentSecond = c.get(Calendar.SECOND);

                    // Alarm için verilen zamandan şu anki zamanı çıkarıp alarmın kaç saniye sonra çalacağını buluyoruz
                    int diff = (alarmHour * 60 + alarmMinute) * 60 - ((currentHour * 60 + currentMinute) * 60 + currentSecond);

                    // Bu süre negatif ise alarm için seçilen zaman şu anki zamandan öncedir bu durumda bu süreye bir gün ekliyoruz
                    if (diff < 0) {
                        diff += 24 * 60 * 60;
                    }

                    // Alarm için kalan süreyi yazı olarak bastırmak için saat, dakika ve saniye cinsinden bu süreyi hesaplıyoruz
                    int h = diff / (60 * 60);
                    int m = diff / 60 - h * 60;
                    int s = diff % 60;

                    String infoText = "";

                    if(h > 0) {
                        infoText += h + " saat";

                        if(m > 0) {
                            infoText += " " + m + " dakika";

                            if(s > 0) {
                                infoText += " " + s + " saniye";
                            }
                        }
                    } else {
                        if(m > 0) {
                            infoText += m + " dakika";

                            if(s > 0) {
                                infoText += " " + s + " saniye";
                            }
                        } else {
                            infoText += s + " saniye";
                        }
                    }

                    // Alarmın zaten kurulu olduğunu ve zaman bilgisini ekrana bildirim olarak basıyoruz
                    Toast.makeText(MainActivity.this, "Alarm zaten kurulu (" + infoText + ")", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Alarm durdurma butonu için tıklanma olayı üretiyoruz
        buttonStop.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if(alarmActive || alarmRunning) {
                    // Alarm aktif veya çalıyorsa şifre ile Alarm iptal için Dialog açıyoruz
                    showPasswordCheckInputDialog();
                } else {
                    // Alarm pasif veya çalmıyorsa  Alarmın kurulmamış olduğunu belirten bir bildirim basıyoruz
                    Toast.makeText(MainActivity.this, "Alarm kurulmamış", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Şifre atama butonu için tıklanma olayı üretiyoruz
        buttonPassword.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Şifre değiştirmek için Dialog açıyoruz
                showPasswordSetInputDialog();
            }
        });

        // Şu anki zamanı alıyoruz
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Şu anki zamanı zaman seçimi için TimePicker'a atıyoruz
        timePicker.clearFocus();
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
    }

    protected void showPasswordCheckInputDialog() {
        // Alarmı durdurmak için bir Dialog ekranı açıyoruz
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.password_check, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.passwordCheckEditPassword);

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Tamam butonuna basıldıysa, girilen şifreyi kontrol ediyoruz
                        if(PASSWORD.equals(editText.getText().toString())) {
                            // Girilen şifre doğru
                            // Eğer alarm aktif ise Alarmı durduracağız, bunu kontrol etmemizin nedeni biz bu rutinde iken Alarmın çalmış ve bitmiş olma ihtimali
                            if(alarmActive) {
                                // Zamanlayıcıyı iptal ediyoruz
                                if (timer != null)
                                    timer.cancel();
                                // Alarm durumunu pasif yapıyoruz
                                alarmActive = false;
                                // Alarm iptal edildi bildirimini ekrana basıyoruz
                                Toast.makeText(MainActivity.this, "Alarm iptal edildi", Toast.LENGTH_SHORT).show();
                            } else if (alarmRunning) {
                                // Alarmı durduruyoruz
                                if(soundPool != null)
                                    soundPool.stop(streamId);

                                alarmRunning = false;
                                streamId = -1;
                                soundPool = null;
                            } else {
                                // Buraya Girmemeli
                                Toast.makeText(MainActivity.this, "Alarm Hatası", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Girilen şifre yanlış
                            if(alarmActive || alarmRunning) {
                                // Alarm durumu aktif ise şifrenin yanlış olduğu bilgisini ekrana basıyoruz
                                Toast.makeText(MainActivity.this, "Şifre yanlış", Toast.LENGTH_SHORT).show();
                            } else {
                                // Buraya Girmemeli
                                Toast.makeText(MainActivity.this, "Alarm Hatası", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Şifre girilme ekranında iptal seçeneği tuşlanırsa şifre kontrolü yapılmadan ekrandan çıkıyoruz, Alarm çalışmaya devam ediyor
                        dialog.cancel();
                    }
                });

        // Dialog ekranını gösteriyoruz
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    protected void showPasswordSetInputDialog() {
        // Alarmı durdurmak için bir Dialog ekranı açıyoruz
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.password_set, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.passwordSetEditPassword);

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Tamam butonuna basıldıysa, girilen şifre uygun mu diye kontrol ediyoruz
                        if(editText.getText().toString().length() >= MIN_PASSWORD_LEN) {
                            // Girilen şifre uygun, atama yapıyoruz
                            PASSWORD = editText.getText().toString();
                            Toast.makeText(MainActivity.this, "Şifre değiştirildi", Toast.LENGTH_SHORT).show();
                        } else {
                            // Girilen şifre uygun değil
                            Toast.makeText(MainActivity.this, "Şifre en az " + MIN_PASSWORD_LEN + " karakterli olmalı", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Şifre atama ekranında iptal seçeneği tuşlanırsa şifre atama işlemi yapılmadan ekrandan çıkıyoruz
                        dialog.cancel();
                    }
                });

        // Dialog ekranını gösteriyoruz
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    class AlarmTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    // Alarm kurulu iken alarm süresi dolunca bu rutine girip tüm Alarm değerlerini temizliyoruz
                    alarmHour = 0;
                    alarmMinute = 0;

                    // Ekrana Alarm bildirimi basıyoruz
                    Toast.makeText(getBaseContext(), "ALARM", Toast.LENGTH_LONG).show();

                    // Alarm sesini çaldırıyoruz
                    soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
                    soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                        @Override
                        public void onLoadComplete(SoundPool soundPool, int soundID, int status) {
                            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                            float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                            float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                            float volume = actualVolume / maxVolume;

                            alarmActive = false;
                            alarmRunning = true;
                            streamId = soundPool.play(soundID, volume, volume, 1, -1, 1.0f);
                        }
                    });
                    soundPool.load(getBaseContext(), R.raw.alarm, 1);
                }
            });
        }
    }

}
