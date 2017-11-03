package gmedia.net.id.psp.PenjualanMKIOS.Adapter;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ItemLength;
import gmedia.net.id.psp.Utils.Status;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListPenjualanMKIOSAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListPenjualanMKIOSAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.cv_list_penjualan_mkios, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvItem1, tvItem2, tvItem3, tvItem4, tvItem5, tvItem6;
    }

    public void addMoreData(List<CustomItem> moreData){

        items.addAll(moreData);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if(items.size() < ItemLength.ListLength){
            return super.getCount();
        }else{
            return ItemLength.ListLength;
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();

        if(convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.cv_list_penjualan_mkios, null);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            holder.tvItem3 = (TextView) convertView.findViewById(R.id.tv_item3);
            holder.tvItem4 = (TextView) convertView.findViewById(R.id.tv_item4);
            holder.tvItem5 = (TextView) convertView.findViewById(R.id.tv_item5);
            holder.tvItem6 = (TextView) convertView.findViewById(R.id.tv_item6);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem1.setText(itemSelected.getItem2());
        holder.tvItem2.setText(itemSelected.getItem3());
        holder.tvItem3.setText(itemSelected.getItem4());
        holder.tvItem4.setText(Html.fromHtml(Status.mkios(itemSelected.getItem6())));
        holder.tvItem5.setText(iv.ChangeFormatDateString(itemSelected.getItem9(), FormatItem.formatTimestamp, FormatItem.formatDateDisplay));
        holder.tvItem6.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(itemSelected.getItem5())));
        return convertView;

    }
}
