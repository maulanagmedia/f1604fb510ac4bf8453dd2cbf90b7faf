package gmedia.net.id.psp.NavKunjungan.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListKunjunganAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListKunjunganAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.adapter_kunjungan_today, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvItem1, tvItem2, tvItem3;
        private LinearLayout llTime;
        private ImageView ivTime;
    }

    public void addMoreData(List<CustomItem> moreData){

        items.addAll(moreData);
        notifyDataSetChanged();
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
            convertView = inflater.inflate(R.layout.adapter_kunjungan_today, null);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            holder.tvItem3 = (TextView) convertView.findViewById(R.id.tv_item3);
            holder.llTime = (LinearLayout) convertView.findViewById(R.id.ll_time);
            holder.ivTime= (ImageView) convertView.findViewById(R.id.iv_time);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);

        if(itemSelected.getItem8().equals("0")){ // bukan pjp tapi terkunjungi

            holder.llTime.setBackgroundColor(context.getResources().getColor(R.color.color_yellow));
            holder.ivTime.setBackground(context.getResources().getDrawable(R.drawable.bg_round_yellow));
        }else if(itemSelected.getItem8().equals("1")){ // pjp terkunjungi
            holder.llTime.setBackgroundColor(context.getResources().getColor(R.color.color_green));
            holder.ivTime.setBackground(context.getResources().getDrawable(R.drawable.bg_round_green));
        }else if(itemSelected.getItem8().equals("2")){ // pjb belum terkunjungi
            holder.llTime.setBackgroundColor(context.getResources().getColor(R.color.color_red));
            holder.ivTime.setBackground(context.getResources().getDrawable(R.drawable.bg_round_red));
        }else{ // terkunjungi tidak belanja
            holder.llTime.setBackgroundColor(context.getResources().getColor(R.color.color_blue));
            holder.ivTime.setBackground(context.getResources().getDrawable(R.drawable.bg_round_blue));
        }

        holder.tvItem2.setText(itemSelected.getItem3());
        if(itemSelected.getItem8().equals("2")){

            holder.tvItem1.setText("");
            holder.tvItem3.setText(itemSelected.getItem4());

        }else{

            holder.tvItem1.setText(iv.ChangeFormatDateString(itemSelected.getItem2(), FormatItem.formatTimestamp, FormatItem.formatTime));
            if(iv.parseNullDouble(itemSelected.getItem4()) < 6371.00){
                String jarak = "";
                if(iv.parseNullDouble(itemSelected.getItem4()) <= 1){
                    jarak = (iv.doubleToString(iv.parseNullDouble(itemSelected.getItem4()) * 1000, "2") + " m");
                }else{
                    jarak = (iv.doubleToString(iv.parseNullDouble(itemSelected.getItem4()), "2") + " km");
                }

                holder.tvItem3.setText(jarak+ " dari lokasi");
            }else if(itemSelected.getItem6().length() == 0){
                holder.tvItem3.setText("Posisi outlet belum tersedia");
            }else{
                holder.tvItem3.setText("Jarak diluar jangkauan");
            }
        }



        return convertView;

    }
}
