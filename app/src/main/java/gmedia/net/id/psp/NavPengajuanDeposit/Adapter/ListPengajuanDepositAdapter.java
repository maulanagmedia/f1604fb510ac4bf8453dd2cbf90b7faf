package gmedia.net.id.psp.NavPengajuanDeposit.Adapter;

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


/**
 * Created by Shin on 1/8/2017.
 */

public class ListPengajuanDepositAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListPengajuanDepositAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.cv_list_pengajuan_deposit, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvItem1, tvItem2, tvItem3, tvItem4, tvItem5;
    }

    public void addMoreData(List<CustomItem> itemsToAdd){

        items.addAll(itemsToAdd);
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
            convertView = inflater.inflate(R.layout.cv_list_pengajuan_deposit, null);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            holder.tvItem3 = (TextView) convertView.findViewById(R.id.tv_item3);
            holder.tvItem4 = (TextView) convertView.findViewById(R.id.tv_item4);
            holder.tvItem5 = (TextView) convertView.findViewById(R.id.tv_item5);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem1.setText(itemSelected.getItem2());
        holder.tvItem2.setText(iv.ChangeToRupiahFormat(itemSelected.getItem3()));
        holder.tvItem3.setText(Html.fromHtml(itemSelected.getItem4()));
        holder.tvItem4.setText(iv.ChangeFormatDateString(itemSelected.getItem5(), FormatItem.formatDate2, FormatItem.formatDateDisplay2));
        holder.tvItem5.setText(itemSelected.getItem7());

        return convertView;

    }
}
