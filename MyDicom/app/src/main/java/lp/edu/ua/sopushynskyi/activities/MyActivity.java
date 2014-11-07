package lp.edu.ua.sopushynskyi.activities;

import android.app.Activity;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mykola.mydicom.R;
import lp.edu.ua.sopushynskyi.components.VerticalSeekBar;

import lp.edu.ua.sopushynskyi.dialogs.OpenFileDialog;
import lp.edu.ua.sopushynskyi.dicom.DCMData;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MyActivity extends Activity {

    private static final String FTYPE = ".dcm";
    ImageView img;
    TextView imgInfo;
    TextView dcmInfo;
    PhotoViewAttacher mAttacher;

    private String mChosenFile;
    private DCMData dcmData = new DCMData();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        Button normal = (Button) findViewById(R.id.normal);
        Button inverse = (Button) findViewById(R.id.inverse);
        Button rainbow = (Button) findViewById(R.id.rainbow);

        Button next = (Button) findViewById(R.id.next);
        Button prev = (Button) findViewById(R.id.previous);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dcmData.nextFrame())
                    redrawImage();
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dcmData.prevFrame())
                    redrawImage();
            }
        });

        img = (ImageView) findViewById(R.id.image);
        imgInfo = (TextView) findViewById(R.id.info);
        dcmInfo = (TextView) findViewById(R.id.metaInfo);

        System.loadLibrary("imebra_lib");

        View.OnClickListener infoPanelListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView textPanel = (TextView)v;
                if(textPanel.getCurrentTextColor() == 0xff00ff00) {
                    textPanel.setTextColor(0x55000000);
                    textPanel.setBackgroundColor(0x00ffffff);
                } else {
                    textPanel.setTextColor(0xff00ff00);
                    textPanel.setBackgroundColor(0x55ffffff);
                }
            }
        };

        dcmInfo.setOnClickListener(infoPanelListener);
        imgInfo.setOnClickListener(infoPanelListener);

        inverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dcmData.inverse();
                redrawImage();
            }
        });

        rainbow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dcmData.rainbow();
                redrawImage();
            }
        });

        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dcmData.normal();
                redrawImage();
            }
        });

        mAttacher = new PhotoViewAttacher(img);

        mAttacher.setOnMatrixChangeListener(new PhotoViewAttacher.OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rectF) {
                printInfo();
            }
        });

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 500);
        layoutParams.setMargins(0, 0, 0, 0);

        VerticalSeekBar contrastBar = new VerticalSeekBar(this);
        contrastBar.setLayoutParams(layoutParams);
        contrastBar.setMax(100);
        contrastBar.setProgress(25);
        contrastBar.setPadding(30, 20, 30, 20);

        contrastBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dcmData.setContrast(progress / 100f * 2 + 0.5f);
                redrawImage();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        RelativeLayout rightPanel = (RelativeLayout)findViewById(R.id.rightPanel);
        RelativeLayout.LayoutParams contrastParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 500);
        contrastParams.addRule(RelativeLayout.BELOW, R.id.icon_contrast);
        rightPanel.addView(contrastBar, contrastParams);

        VerticalSeekBar brightnessBar = new VerticalSeekBar(this);
        brightnessBar.setLayoutParams(layoutParams);
        brightnessBar.setMax(100);
        brightnessBar.setProgress(50);
        brightnessBar.setPadding(30, 20, 30, 20);

        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dcmData.setBrightness((int)(progress / 100f * 500) - 250);
                redrawImage();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        RelativeLayout.LayoutParams brightnessParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 500);
        brightnessParams.addRule(RelativeLayout.BELOW, R.id.icon_brightness);
        RelativeLayout leftPanel = (RelativeLayout)findViewById(R.id.leftPanel);
        leftPanel.addView(brightnessBar, brightnessParams);
    }

    private void redrawImage() {
        if (dcmData.isLoaded()) {
            img.setImageBitmap(dcmData.getFrame());
            printInfo();
        }
    }

    private void printInfo() {
        String info = String.format(
                "Схема   : %s\n" +
                "Контраст: %.2f\n" +
                "Яскрав. : %d\n" +
                "Масштаб : %.2f%%\n" +
                "Кадр    : %d/%d",
                dcmData.getColorSchema(),
                dcmData.getContrast(),
                dcmData.getBrightness(),
                mAttacher.getScale() * 100,
                dcmData.getCurrentFrame() + 1,
                dcmData.getFramesNumber());

        imgInfo.setText(info);

        String metaInfo = dcmData.getMetaInfo();
        dcmInfo.setText(metaInfo);
    }

    private void loadDCM(String fileName) {
        dcmData.loadDCM(fileName);
        redrawImage();
        mAttacher.update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                OpenFileDialog fileDialog = new OpenFileDialog(this).setFilter(FTYPE).setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
                    @Override
                    public void OnSelectedFile(String fileName) {
                        mChosenFile = fileName;
                        setTitle(mChosenFile);
                        Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_LONG).show();
                        loadDCM(fileName);
                    }
                });
                fileDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}