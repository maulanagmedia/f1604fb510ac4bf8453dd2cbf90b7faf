package gmedia.net.id.psp.NavMarketSurveyMD.Adapter;

import android.app.Activity;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ItemLength;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListDisplayMDAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListDisplayMDAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.cv_display_belanja_md, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvText1;
        private EditText edtText1, edtText2, edtText3, edtText4;
        private EditText edtText5, edtText6, edtText7, edtText8;
        private LinearLayout llContainer;
        private TextView tvTitle1, tvTitle2, tvTitle3, tvTitle4, tvTitle5, tvTitle6, tvTitle7, tvTitle8, tvTitle9, tvTitle10;
    }

    public List<CustomItem> getItems(){

        return items;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();

        if(convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.cv_display_belanja_md, null);
            holder.tvText1 = (TextView) convertView.findViewById(R.id.tv_text1);
            holder.edtText1 = (EditText) convertView.findViewById(R.id.edt_text1);
            holder.edtText2 = (EditText) convertView.findViewById(R.id.edt_text2);
            holder.edtText3 = (EditText) convertView.findViewById(R.id.edt_text3);
            holder.edtText4 = (EditText) convertView.findViewById(R.id.edt_text4);
            holder.edtText5 = (EditText) convertView.findViewById(R.id.edt_text5);
            holder.edtText6 = (EditText) convertView.findViewById(R.id.edt_text6);
            holder.edtText7 = (EditText) convertView.findViewById(R.id.edt_text7);
            holder.edtText8 = (EditText) convertView.findViewById(R.id.edt_text8);
            holder.llContainer = (LinearLayout) convertView.findViewById(R.id.ll_container);

            holder.tvTitle1 = (TextView) convertView.findViewById(R.id.tv_title1);
            holder.tvTitle2 = (TextView) convertView.findViewById(R.id.tv_title2);
            holder.tvTitle3 = (TextView) convertView.findViewById(R.id.tv_title3);
            holder.tvTitle4 = (TextView) convertView.findViewById(R.id.tv_title4);
            holder.tvTitle5 = (TextView) convertView.findViewById(R.id.tv_title5);
            holder.tvTitle6 = (TextView) convertView.findViewById(R.id.tv_title6);
            holder.tvTitle7 = (TextView) convertView.findViewById(R.id.tv_title7);
            holder.tvTitle8 = (TextView) convertView.findViewById(R.id.tv_title8);
            holder.tvTitle9 = (TextView) convertView.findViewById(R.id.tv_title9);
            holder.tvTitle10 = (TextView) convertView.findViewById(R.id.tv_title10);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvText1.setText(itemSelected.getItem2());

        final ViewHolder finalHolder = holder;
        finalHolder.edtText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {
                    items.get(position).setItem8(editable.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        finalHolder.edtText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {
                    items.get(position).setItem9(editable.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        finalHolder.edtText3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {
                    items.get(position).setItem10(editable.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        finalHolder.edtText4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {
                    items.get(position).setItem4(editable.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        finalHolder.edtText5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {
                    items.get(position).setItem11(editable.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        finalHolder.edtText6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {
                    items.get(position).setItem12(editable.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        finalHolder.edtText7.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {
                    items.get(position).setItem13(editable.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        finalHolder.edtText8.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {
                    items.get(position).setItem7(editable.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        holder.edtText1.setText(itemSelected.getItem8());
        holder.edtText2.setText(itemSelected.getItem9());
        holder.edtText3.setText(itemSelected.getItem10());
        holder.edtText4.setText(itemSelected.getItem4());
        holder.edtText5.setText(itemSelected.getItem11());
        holder.edtText6.setText(itemSelected.getItem12());
        holder.edtText7.setText(itemSelected.getItem13());
        holder.edtText8.setText(itemSelected.getItem7());

        holder.llContainer.setBackgroundColor(Color.parseColor(itemSelected.getItem14()));
        holder.tvText1.setTextColor(Color.parseColor(itemSelected.getItem15()));
        holder.tvTitle1.setTextColor(Color.parseColor(itemSelected.getItem15()));
        holder.tvTitle2.setTextColor(Color.parseColor(itemSelected.getItem15()));
        holder.tvTitle3.setTextColor(Color.parseColor(itemSelected.getItem15()));
        holder.tvTitle4.setTextColor(Color.parseColor(itemSelected.getItem15()));
        holder.tvTitle5.setTextColor(Color.parseColor(itemSelected.getItem15()));
        holder.tvTitle6.setTextColor(Color.parseColor(itemSelected.getItem15()));
        holder.tvTitle7.setTextColor(Color.parseColor(itemSelected.getItem15()));
        holder.tvTitle8.setTextColor(Color.parseColor(itemSelected.getItem15()));
        holder.tvTitle9.setTextColor(Color.parseColor(itemSelected.getItem15()));
        holder.tvTitle10.setTextColor(Color.parseColor(itemSelected.getItem15()));

        return convertView;

    }
}
