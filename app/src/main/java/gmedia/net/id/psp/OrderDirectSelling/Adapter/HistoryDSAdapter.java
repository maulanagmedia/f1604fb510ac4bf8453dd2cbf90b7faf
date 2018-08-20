package gmedia.net.id.psp.OrderDirectSelling.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ItemLength;


/**
 * Created by Shin on 1/8/2017.
 */

public class HistoryDSAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public HistoryDSAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.cv_riwayat_ds, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvItem1, tvItem2, tvItem3, tvItem4, tvItem5, tvItem6, tvItem7, tvItem8;
        private LinearLayout ll3;
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
            convertView = inflater.inflate(R.layout.cv_riwayat_ds, null);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            holder.tvItem3 = (TextView) convertView.findViewById(R.id.tv_item3);
            holder.tvItem4 = (TextView) convertView.findViewById(R.id.tv_item4);
            holder.tvItem5 = (TextView) convertView.findViewById(R.id.tv_item5);
            holder.tvItem6 = (TextView) convertView.findViewById(R.id.tv_item6);
            holder.tvItem7 = (TextView) convertView.findViewById(R.id.tv_item7);
            holder.tvItem8 = (TextView) convertView.findViewById(R.id.tv_item8);
            holder.ll3 = (LinearLayout) convertView.findViewById(R.id.ll_3);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem1.setText(itemSelected.getItem7());
        holder.tvItem2.setText(itemSelected.getItem8());
        holder.tvItem3.setText(itemSelected.getItem9());
        holder.tvItem4.setText(itemSelected.getItem3());
        holder.tvItem5.setText(itemSelected.getItem6());
        holder.tvItem6.setText(iv.ChangeToCurrencyFormat(itemSelected.getItem5()));
        holder.tvItem7.setText(itemSelected.getItem10());
        holder.tvItem8.setText(itemSelected.getItem11());
        return convertView;

    }
}
