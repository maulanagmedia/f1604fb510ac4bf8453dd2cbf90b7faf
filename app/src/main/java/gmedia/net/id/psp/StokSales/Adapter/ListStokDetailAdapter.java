package gmedia.net.id.psp.StokSales.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.HashMap;
import java.util.List;

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ItemLength;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListStokDetailAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();
    private HashMap<String, List<CustomItem>> detailList;

    public ListStokDetailAdapter(Activity context, List<CustomItem> items, HashMap<String, List<CustomItem>> detailList) {
        super(context, R.layout.cv_list_stok_detail, items);
        this.context = context;
        this.items = items;
        this.detailList = detailList;
    }

    private static class ViewHolder {
        private TextView tvItem1, tvItem2, tvItem3, tvItem4;
        private ListView lvTerjual;
    }

    public void addMoreData(List<CustomItem> moreData, HashMap<String, List<CustomItem>> detailList){
        items.addAll(moreData);
        this.detailList = detailList;
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
            convertView = inflater.inflate(R.layout.cv_list_stok_detail, null);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            holder.tvItem3 = (TextView) convertView.findViewById(R.id.tv_item3);
            holder.tvItem4 = (TextView) convertView.findViewById(R.id.tv_item4);
            holder.lvTerjual = (ListView) convertView.findViewById(R.id.lv_terjual);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem1.setText(itemSelected.getItem2());
        holder.tvItem2.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(itemSelected.getItem4())));
        holder.tvItem3.setText(String.valueOf(iv.parseNullInteger(itemSelected.getItem5()) - iv.parseNullInteger(itemSelected.getItem3())));
        holder.tvItem4.setText(itemSelected.getItem3());
        List<CustomItem> detail = detailList.get(itemSelected.getItem6());
        holder.lvTerjual.setAdapter(null);
        if(detail != null && detail.size() > 0){
            DetailTerjualAdapter adapter = new DetailTerjualAdapter(context, detail);
            holder.lvTerjual.setAdapter(adapter);
        }
        return convertView;

    }
}
