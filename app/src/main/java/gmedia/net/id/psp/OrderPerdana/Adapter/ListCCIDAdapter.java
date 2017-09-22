package gmedia.net.id.psp.OrderPerdana.Adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.psp.OrderPerdana.DetailOrderPerdana;
import gmedia.net.id.psp.R;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListCCIDAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListCCIDAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.cv_list_ccid, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvItem1, tvItem2, tvItem3;
        private ImageView ivDelete;
    }

    public void changeData(String ccid, String harga){

        int x = 0;
        for(CustomItem item: items){

            if(item.getItem2().equals(ccid)){

                items.get(x).setItem4(harga);
                notifyDataSetChanged();
                break;
            }
            x++;
        }
    }

    public List<CustomItem> getDataList(){

        return items;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();

        if(convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.cv_list_ccid, null);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            holder.tvItem3 = (TextView) convertView.findViewById(R.id.tv_item3);
            holder.ivDelete = (ImageView) convertView.findViewById(R.id.iv_delete);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem1.setText((position + 1)+"");
        holder.tvItem2.setText(itemSelected.getItem2());
        holder.tvItem3.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(itemSelected.getItem4())));
        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Konfirmasi")
                        .setMessage("Apakah anda yakin ingin menghapus " + itemSelected.getItem2())
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                String ccid = itemSelected.getItem2();
                                items.remove(position);
                                notifyDataSetChanged();
                                DetailOrderPerdana.deleteSelectedCCID(ccid);
                            }
                        })
                        .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
            }
        });
        return convertView;

    }
}
