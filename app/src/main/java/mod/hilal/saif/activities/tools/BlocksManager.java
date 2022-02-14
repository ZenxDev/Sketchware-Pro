package mod.hilal.saif.activities.tools;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sketchware.remod.Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import a.a.a.Zx;
import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.hey.studios.editor.manage.block.v2.BlockLoader;
import mod.hey.studios.util.Helper;
import mod.hilal.saif.lib.PCP;
import mod.jbk.util.LogUtil;

public class BlocksManager extends AppCompatActivity {

    private final ArrayList<HashMap<String, Object>> temp_list = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> all_blocks_list = new ArrayList<>();
    private String blocks_dir = "";
    private String pallet_dir = "";
    private double insert_n = 0;
    private ArrayList<HashMap<String, Object>> pallet_listmap = new ArrayList<>();
    private ListView listview1;
    private LinearLayout card2;
    private TextView card2_sub;

    @Override
    public void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(Resources.layout.blocks_manager);
        initialize();
        initializeLogic();
    }

    @Override
    public void onStop() {
        super.onStop();

        BlockLoader.refresh();
    }

    private void initialize() {
        FloatingActionButton _fab = findViewById(Resources.id.fab);
        listview1 = findViewById(Resources.id.list_pallete);
        ImageView back_icon = findViewById(Resources.id.back_icon);
        ImageView arrange_icon = findViewById(Resources.id.dirs);
        card2 = findViewById(Resources.id.recycle_bin);
        card2_sub = findViewById(Resources.id.recycle_sub);

        back_icon.setOnClickListener(Helper.getBackPressedClickListener(this));

        arrange_icon.setOnClickListener(v -> {
            final AlertDialog dialog = new AlertDialog.Builder(BlocksManager.this).create();
            LayoutInflater inflater = getLayoutInflater();
            final View convertView = inflater.inflate(Resources.layout.settings_popup, null);
            dialog.setView(convertView);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            final EditText pallet = convertView.findViewById(Resources.id.pallet_dir);
            pallet.setText(pallet_dir.replace(FileUtil.getExternalStorageDir(), ""));
            final EditText block = convertView.findViewById(Resources.id.blocks_dir);
            block.setText(blocks_dir.replace(FileUtil.getExternalStorageDir(), ""));
            final TextInputLayout extra = convertView.findViewById(Resources.id.extra_input_layout);
            extra.setVisibility(View.GONE);
            final TextView save = convertView.findViewById(Resources.id.save);
            final TextView cancel = convertView.findViewById(Resources.id.cancel);
            final TextView de = convertView.findViewById(Resources.id.defaults);
            save.setOnClickListener(saveView -> {
                ConfigActivity.setSetting(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH,
                        pallet.getText().toString());
                ConfigActivity.setSetting(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH,
                        block.getText().toString());

                _readSettings();
                _refresh_list();
                dialog.dismiss();
            });
            cancel.setOnClickListener(Helper.getDialogDismissListener(dialog));
            de.setOnClickListener(defaultsView -> {
                ConfigActivity.setSetting(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH,
                        "/.sketchware/resources/block/My Block/palette.json");
                ConfigActivity.setSetting(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH,
                        "/.sketchware/resources/block/My Block/block.json");

                _readSettings();
                _refresh_list();
                dialog.dismiss();
            });
            dialog.show();
        });

        _fab.setOnClickListener(v -> {
            insert_n = -1;
            final AlertDialog dialog = new AlertDialog.Builder(BlocksManager.this).create();
            LayoutInflater inflater = getLayoutInflater();
            final View convertView = inflater.inflate(Resources.layout.add_new_pallete_customview, null);
            dialog.setView(convertView);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            final EditText name = convertView.findViewById(Resources.id.name);
            final EditText colour = convertView.findViewById(Resources.id.color);
            final TextView save = convertView.findViewById(Resources.id.save);
            final TextView cancel = convertView.findViewById(Resources.id.cancel);
            final ImageView select = convertView.findViewById(Resources.id.select);

            select.setOnClickListener(getSharedPaletteColorPickerShower(dialog, colour));

            save.setOnClickListener(saveView -> {
                try {
                    Color.parseColor(colour.getText().toString());

                    _createPallette(name.getText().toString(), colour.getText().toString());
                    insert_n = -1;
                    dialog.dismiss();
                } catch (IllegalArgumentException e) {
                    colour.setError("Malformed hexadecimal color");
                    colour.requestFocus();
                }
            });
            cancel.setOnClickListener(cancelView -> {
                insert_n = -1;
                dialog.dismiss();
            });
            dialog.show();
        });
    }

    private void initializeLogic() {
        _readSettings();
        _refresh_list();
        _recycleBin(card2);
        insert_n = -1;
    }

    @Override
    public void onResume() {
        super.onResume();

        _readSettings();
        _refresh_list();
    }

    private void _a(final View _view) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setColor(Color.parseColor("#ffffff"));
        RippleDrawable rippleDrawable = new RippleDrawable(new ColorStateList(new int[][]{new int[0]}, new int[]{Color.parseColor("#20008DCD")}), gradientDrawable, null);
        if (Build.VERSION.SDK_INT >= 21) {
            _view.setBackground(rippleDrawable);
            _view.setClickable(true);
            _view.setFocusable(true);
        }
    }

    private void _readSettings() {
        pallet_dir = FileUtil.getExternalStorageDir() + ConfigActivity.getStringSettingValueOrSetAndGet(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH,
                "/.sketchware/resources/block/My Block/palette.json");
        blocks_dir = FileUtil.getExternalStorageDir() + ConfigActivity.getStringSettingValueOrSetAndGet(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH,
                "/.sketchware/resources/block/My Block/block.json");

        if (FileUtil.isExistFile(blocks_dir)) {
            try {
                all_blocks_list = new Gson().fromJson(FileUtil.readFile(blocks_dir), Helper.TYPE_MAP_LIST);
            } catch (JsonParseException e) {
                SketchwareUtil.toastError("Failed to parse " + blocks_dir + ", using none.");
                LogUtil.e("BlocksManager", "Failed to parse " + blocks_dir, e);
            }
        }
    }

    private void _refresh_list() {
        try {
            if (FileUtil.isExistFile(pallet_dir) && !FileUtil.readFile(pallet_dir).equals("")) {
                Parcelable savedState = listview1.onSaveInstanceState();
                pallet_listmap = new Gson().fromJson(FileUtil.readFile(pallet_dir), Helper.TYPE_MAP_LIST);
                listview1.setAdapter(new PaletteAdapter(pallet_listmap));
                ((BaseAdapter) listview1.getAdapter()).notifyDataSetChanged();
                listview1.onRestoreInstanceState(savedState);
            } else {
                pallet_listmap.clear();
                listview1.setAdapter(new PaletteAdapter(pallet_listmap));
                ((BaseAdapter) listview1.getAdapter()).notifyDataSetChanged();
            }
            card2_sub.setText("Blocks: " + (long) (_getN(-1)));
        } catch (Exception e) {
        }
    }

    private void _remove_pallete(final double _p) {
        new AlertDialog.Builder(this)
                .setTitle(pallet_listmap.get((int) _p).get("name").toString())
                .setMessage("Remove all blocks related to this palette?")
                .setPositiveButton("Remove permanently", (dialog, which) -> {
                    pallet_listmap.remove((int) (_p));
                    FileUtil.writeFile(pallet_dir, new Gson().toJson(pallet_listmap));
                    _removeRelatedBlocks(_p + 9);
                    _readSettings();
                    _refresh_list();
                })
                .setNegativeButton(Resources.string.common_word_cancel, null)
                .setNeutralButton("Move to recycle bin", (dialog, which) -> {
                    _moveRelatedBlocksToRecycleBin(_p + 9);
                    pallet_listmap.remove((int) (_p));
                    FileUtil.writeFile(pallet_dir, new Gson().toJson(pallet_listmap));
                    _removeRelatedBlocks(_p + 9);
                    _readSettings();
                    _refresh_list();
                }).show();
    }

    private double _getN(final double _p) {
        int n = 0;
        for (int i = 0; i < all_blocks_list.size(); i++) {
            if (all_blocks_list.get(i).get("palette").toString().equals(String.valueOf((long) (_p)))) {
                n++;
            }
        }
        return (n);
    }

    private void _createPallette(final String _name, final String _colour) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", _name);
        map.put("color", _colour);

        if (insert_n == -1) {
            pallet_listmap.add(map);
            FileUtil.writeFile(pallet_dir, new Gson().toJson(pallet_listmap));
            _readSettings();
            _refresh_list();
        } else {
            pallet_listmap.add((int) insert_n, map);
            FileUtil.writeFile(pallet_dir, new Gson().toJson(pallet_listmap));
            _readSettings();
            _refresh_list();
            _insertBlocksAt(insert_n + 9);
        }
        insert_n = -1;
    }

    private void _showEditDial(final double _p, final String _name, final String _c) {
        final AlertDialog dialog = new AlertDialog.Builder(BlocksManager.this).create();
        LayoutInflater inflater = getLayoutInflater();
        final View convertView = inflater.inflate(Resources.layout.add_new_pallete_customview, null);
        dialog.setView(convertView);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final EditText name = convertView.findViewById(Resources.id.name);
        name.setText(_name);
        final EditText colour = convertView.findViewById(Resources.id.color);
        colour.setText(_c);
        final TextView title = convertView.findViewById(Resources.id.title);
        title.setText("Edit palette");
        final TextView save = convertView.findViewById(Resources.id.save);
        final TextView cancel = convertView.findViewById(Resources.id.cancel);
        final ImageView select = convertView.findViewById(Resources.id.select);
        select.setOnClickListener(getSharedPaletteColorPickerShower(dialog, colour));
        save.setOnClickListener(v -> {
            try {
                Color.parseColor(colour.getText().toString());
                _editPallete(_p, name.getText().toString(), colour.getText().toString());
                dialog.dismiss();
            } catch (IllegalArgumentException e) {
                colour.setError("Malformed hexadecimal color");
                colour.requestFocus();
            }
        });
        cancel.setOnClickListener(Helper.getDialogDismissListener(dialog));
        dialog.show();
    }

    private void _editPallete(final double _p, final String _n, final String _c) {
        pallet_listmap.get((int) _p).put("name", _n);
        pallet_listmap.get((int) _p).put("color", _c);
        FileUtil.writeFile(pallet_dir, new Gson().toJson(pallet_listmap));
        _readSettings();
        _refresh_list();
    }

    private void _MoveUp(final double _p) {
        if (_p > 0) {
            Collections.swap(pallet_listmap, (int) (_p), (int) (_p + -1));

            Parcelable savedState = listview1.onSaveInstanceState();
            FileUtil.writeFile(pallet_dir, new Gson().toJson(pallet_listmap));
            _swapRelatedBlocks(_p + 9, _p + 8);
            _readSettings();
            _refresh_list();
            listview1.onRestoreInstanceState(savedState);
        }
    }

    private void _recycleBin(final View _v) {
        _a(_v);
        card2.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), BlocksManagerDetailsActivity.class);
            intent.putExtra("position", "-1");
            intent.putExtra("dirB", blocks_dir);
            intent.putExtra("dirP", pallet_dir);
            startActivity(intent);
        });
        card2.setOnLongClickListener(v -> {
            new AlertDialog.Builder(BlocksManager.this)
                    .setTitle("Recycle bin")
                    .setMessage("Are you sure you want to empty the recycle bin? " +
                            "Blocks inside will be deleted PERMANENTLY, you CANNOT recover them!")
                    .setPositiveButton("Empty", (dialog, which) -> _emptyRecyclebin())
                    .setNegativeButton(Resources.string.common_word_cancel, null)
                    .show();
            return true;
        });
    }

    private void _insert_pallete(final double _p) {
        insert_n = _p;

        final AlertDialog dialog = new AlertDialog.Builder(BlocksManager.this).create();
        LayoutInflater inflater = getLayoutInflater();
        final View convertView = inflater.inflate(Resources.layout.add_new_pallete_customview, null);
        dialog.setView(convertView);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final EditText name = convertView.findViewById(Resources.id.name);
        final EditText colour = convertView.findViewById(Resources.id.color);
        final TextView save = convertView.findViewById(Resources.id.save);
        final TextView cancel = convertView.findViewById(Resources.id.cancel);
        final ImageView select = convertView.findViewById(Resources.id.select);
        select.setOnClickListener(getSharedPaletteColorPickerShower(dialog, colour));
        save.setOnClickListener(v -> {
            try {
                Color.parseColor(colour.getText().toString());
                _createPallette(name.getText().toString(), colour.getText().toString());
                dialog.dismiss();
            } catch (IllegalArgumentException e) {
                colour.setError("Malformed hexadecimal color");
                colour.requestFocus();
            }
        });
        cancel.setOnClickListener(Helper.getDialogDismissListener(dialog));
        dialog.show();
    }

    private void _moveDown(final double _p) {
        if (_p < (pallet_listmap.size() - 1)) {
            Collections.swap(pallet_listmap, (int) (_p), (int) (_p + 1));
            {
                Parcelable savedState = listview1.onSaveInstanceState();
                FileUtil.writeFile(pallet_dir, new Gson().toJson(pallet_listmap));
                _swapRelatedBlocks(_p + 9, _p + 10);
                _readSettings();
                _refresh_list();
                listview1.onRestoreInstanceState(savedState);
            }
        }
    }

    private void _removeRelatedBlocks(final double _p) {
        temp_list.clear();
        for (int i = 0; i < all_blocks_list.size(); i++) {
            if (!(Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) == _p)) {
                if (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) > _p) {
                    HashMap<String, Object> m = all_blocks_list.get(i);
                    m.put("palette", String.valueOf((long) (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) - 1)));
                    temp_list.add(m);
                } else {
                    temp_list.add(all_blocks_list.get(i));
                }
            }
        }
        FileUtil.writeFile(blocks_dir, new Gson().toJson(temp_list));
        _readSettings();
        _refresh_list();
    }

    private void _swapRelatedBlocks(final double _f, final double _s) {
        for (int i = 0; i < all_blocks_list.size(); i++) {
            if (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) == _f) {
                all_blocks_list.get(i).put("palette", "123456789");
            }
            if (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) == _s) {
                all_blocks_list.get(i).put("palette", String.valueOf((long) (_f)));
            }
        }

        for (int i = 0; i < all_blocks_list.size(); i++) {
            if (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) == 123456789) {
                all_blocks_list.get(i).put("palette", String.valueOf((long) (_s)));
            }
        }
        FileUtil.writeFile(blocks_dir, new Gson().toJson(all_blocks_list));
        _readSettings();
        _refresh_list();
    }

    private void _insertBlocksAt(final double _p) {
        for (int i = 0; i < all_blocks_list.size(); i++) {
            if ((Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) > _p) || (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) == _p)) {
                all_blocks_list.get(i).put("palette", String.valueOf((long) (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) + 1)));
            }
        }
        FileUtil.writeFile(blocks_dir, new Gson().toJson(all_blocks_list));
        _readSettings();
        _refresh_list();
    }

    private void _moveRelatedBlocksToRecycleBin(final double _p) {
        for (int i = 0; i < all_blocks_list.size(); i++) {
            if (Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) == _p) {
                all_blocks_list.get(i).put("palette", "-1");
            }
        }
        FileUtil.writeFile(blocks_dir, new Gson().toJson(all_blocks_list));
        _readSettings();
        _refresh_list();
    }

    private void _emptyRecyclebin() {
        temp_list.clear();
        for (int i = 0; i < all_blocks_list.size(); i++) {
            if (!(Double.parseDouble(all_blocks_list.get(i).get("palette").toString()) == -1)) {
                temp_list.add(all_blocks_list.get(i));
            }
        }
        FileUtil.writeFile(blocks_dir, new Gson().toJson(temp_list));
        _readSettings();
        _refresh_list();
    }

    private View.OnClickListener getSharedPaletteColorPickerShower(AlertDialog dialog, EditText storePickedResultIn) {
        return v -> {
            LayoutInflater inf = getLayoutInflater();
            final View a = inf.inflate(Resources.layout.color_picker, null);
            final Zx zx = new Zx(a, BlocksManager.this, 0, true, false);
            zx.a(new PCP(BlocksManager.this, storePickedResultIn, dialog));
            zx.setAnimationStyle(Resources.anim.abc_fade_in);
            zx.showAtLocation(a, Gravity.CENTER, 0, 0);
            dialog.hide();
        };
    }

    public class PaletteAdapter extends BaseAdapter {

        private final ArrayList<HashMap<String, Object>> palettes;

        public PaletteAdapter(ArrayList<HashMap<String, Object>> palettes) {
            this.palettes = palettes;
        }

        @Override
        public int getCount() {
            return palettes.size();
        }

        @Override
        public HashMap<String, Object> getItem(int position) {
            return palettes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater _inflater = getLayoutInflater();
            if (convertView == null) {
                convertView = _inflater.inflate(Resources.layout.pallet_customview, null);
            }

            final LinearLayout background = convertView.findViewById(Resources.id.background);
            final LinearLayout color = convertView.findViewById(Resources.id.color);
            final TextView title = convertView.findViewById(Resources.id.title);
            final TextView sub = convertView.findViewById(Resources.id.sub);

            title.setText(pallet_listmap.get(position).get("name").toString());
            sub.setText("Blocks: " + (long) (_getN(position + 9)));
            card2_sub.setText("Blocks: " + (long) (_getN(-1)));
            color.setBackgroundColor(Color.parseColor((String) palettes.get(position).get("color")));
            _a(background);
            background.setOnLongClickListener(v -> {
                final String moveUp = "Move up";
                final String moveDown = "Move down";
                final String edit = "Edit";
                final String delete = "Delete";
                final String insert = "Insert";

                PopupMenu popup = new PopupMenu(BlocksManager.this, background);
                Menu menu = popup.getMenu();
                if (position != 0) menu.add(moveUp);
                if (position != getCount() - 1) menu.add(moveDown);
                menu.add(edit);
                menu.add(delete);
                menu.add(insert);
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getTitle().toString()) {
                        case edit:
                            _showEditDial(position, pallet_listmap.get(position).get("name").toString(),
                                    pallet_listmap.get(position).get("color").toString());
                            break;

                        case delete:
                            _remove_pallete(position);
                            break;

                        case moveUp:
                            _MoveUp(position);
                            break;

                        case moveDown:
                            _moveDown(position);
                            break;

                        case insert:
                            _insert_pallete(position);
                            break;

                        default:
                    }
                    return true;
                });
                popup.show();

                return true;
            });

            background.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), BlocksManagerDetailsActivity.class);
                intent.putExtra("position", String.valueOf((long) (position + 9)));
                intent.putExtra("dirB", blocks_dir);
                intent.putExtra("dirP", pallet_dir);
                startActivity(intent);
            });

            return convertView;
        }
    }
}
