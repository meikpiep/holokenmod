package com.holokenmod.ui;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.holokenmod.ApplicationPreferences;
import com.holokenmod.R;
import com.holokenmod.SaveGame;
import com.holokenmod.Theme;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class SaveGameListAdapter extends BaseAdapter {
    
    public ArrayList<String> mGameFiles;
    private LayoutInflater inflater;
    private SaveGameListActivity mContext;
    //private Typeface mFace;

    public SharedPreferences preferences;

    public SaveGameListAdapter(SaveGameListActivity context) {
        this.inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mGameFiles = new ArrayList<String>();
        this.refreshFiles();
    }
    
    public class SortSavedGames implements Comparator<String> {
        long save1 = 0;
        long save2 = 0;
        public int compare(String object1, String object2) {
            try {
                save1 = new SaveGame(mContext.getFilesDir().getPath() + "/" + object1).ReadDate();
                save2 = new SaveGame(mContext.getFilesDir().getPath() + "/" + object2).ReadDate();
            }
            catch (Exception e) {
                //
            }
            return (int) ((save2 - save1)/1000);
        }
        
    }
    
    public void refreshFiles() {
        this.mGameFiles.clear();
        File dir = mContext.getFilesDir();
        String[] allFiles = dir.list();
        for (String entryName : allFiles)
            if (entryName.startsWith("savegame_"))
                this.mGameFiles.add(entryName);
        Collections.sort(this.mGameFiles, new SortSavedGames());
    }

    public int getCount() {
        return this.mGameFiles.size();
    }

    public Object getItem(int arg0) {
        //if (arg0 == 0)
        //    return "";
        return this.mGameFiles.get(arg0);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.object_savegame, null);

        GridUI grid = (GridUI)convertView.findViewById(R.id.saveGridView);
        TextView gametitle = (TextView)convertView.findViewById(R.id.saveGameTitle);
        TextView datetime = (TextView)convertView.findViewById(R.id.saveDateTime);

        final String saveFile = mContext.getFilesDir().getPath() + "/"+ this.mGameFiles.get(position);
        
        this.preferences = PreferenceManager.getDefaultSharedPreferences(convertView.getContext());
        grid.mContext = this.mContext;
        grid.mActive = false;

        Theme theme = ApplicationPreferences.getInstance().getTheme();

        convertView.findViewById(R.id.saveGameRow).setBackgroundColor(
                theme.getBackgroundColor());
        gametitle.setTextColor(theme.getTextColor());
        datetime.setTextColor(theme.getTextColor());

        SaveGame saver = new SaveGame(saveFile);
        try {
            saver.Restore(grid);
        }
        catch (Exception e) {
            // Error, delete the file.
            new File(saveFile).delete();
            return convertView;
        }
        grid.setBackgroundColor(0xFFFFFFFF);
        for (GridCellUI cell : grid.mCells)
            cell.getCell().setSelected(false);
        
        long millis = grid.mPlayTime;
        gametitle.setText(String.format("%dx%d - ", grid.getGrid().getGridSize(), 
                grid.getGrid().getGridSize()) + Utils.convertTimetoStr(millis));
        
        Calendar gameDateTime = Calendar.getInstance();
        gameDateTime.setTimeInMillis(grid.mDate);
        datetime.setText("" + DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.SHORT).format(grid.mDate));
        
        ImageButton loadButton = (ImageButton)convertView.findViewById(R.id.button_play);
        loadButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mContext.loadSaveGame(saveFile);
            }
        });
        
        ImageButton deleteButton = (ImageButton)convertView.findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mContext.deleteGameDialog(saveFile);
            }
        });
        
        return convertView;
    }
}