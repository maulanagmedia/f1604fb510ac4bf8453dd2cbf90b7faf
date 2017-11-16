package gmedia.net.id.psp.InfoDeposit.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.psp.R;

/**
 * Created by indra on 29/12/2016.
 */

public class DepositAdapter extends ArrayAdapter {

    private Context context;
    private List<CustomItem> items;
    private View viewInflater;
    private ItemValidation iv = new ItemValidation();

    public DepositAdapter(Context context, List<CustomItem> items) {
        super(context, R.layout.adapter_penjualan_header, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    private static class ViewHolder {
        private TextView tvItem1, tvItem2 ;
    }

    @Override
    public int getItemViewType(int position) {

        int hasil = 0;
        final CustomItem item = items.get(position);
        String title = item.getItem1();
        if(title.equals("H")){
            hasil = 0;
        }else if(title.equals("I")){
            hasil = 1;
        }else{
            hasil = 2;
        }

        return hasil;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();
        final CustomItem item = items.get(position);
        int tipeViewList = getItemViewType(position);

        if(convertView == null){

            LayoutInflater inflater = ((Activity)context).getLayoutInflater();

            if(tipeViewList == 0){
                convertView = inflater.inflate(R.layout.adapter_penjualan_header, null);
                holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            }else if(tipeViewList == 1){
                convertView = inflater.inflate(R.layout.adapter_penjualan_isi, null);
                holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
                holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            }else if(tipeViewList == 2){
                convertView = inflater.inflate(R.layout.adapter_penjualan_footer, null);
                holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            }

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        if(tipeViewList == 0){
            holder.tvItem1.setText(item.getItem2());
        }else if(tipeViewList == 1){
            holder.tvItem1.setText(item.getItem3());
            holder.tvItem2.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getItem4())));
        }else if(tipeViewList == 2){
            holder.tvItem1.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getItem2())));
        }

        return convertView;
    }
}
