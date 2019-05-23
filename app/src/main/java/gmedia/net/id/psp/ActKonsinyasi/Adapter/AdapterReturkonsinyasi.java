package gmedia.net.id.psp.ActKonsinyasi.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.psp.R;

public class AdapterReturkonsinyasi extends RecyclerView.Adapter<AdapterReturkonsinyasi.PiutangViewHolder> {

    private List<CustomItem> listItem;
    private ItemValidation iv = new ItemValidation();
    private Context context;

    public AdapterReturkonsinyasi(List<CustomItem> listItem, Context context){
        this.listItem = listItem;
        this.context = context;
    }

    @NonNull
    @Override
    public PiutangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_retur_konsinyasi, parent, false);
        return new PiutangViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PiutangViewHolder holder, int position) {

        final CustomItem item = listItem.get(position);
        holder.tvItem1.setText(item.getItem2());
        holder.tvItem2.setText(item.getItem3());
        holder.tvItem3.setText(item.getItem4());
        holder.tvItem4.setText(item.getItem5());

        holder.cvContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Intent intent = new Intent(context, DetailPelunasanPiutang.class);
                intent.putExtra("id", outlet.getItem1());
                intent.putExtra("kdcus", outlet.getItem6());
                intent.putExtra("nama", outlet.getItem2());
                context.startActivity(intent);*/
            }
        });
    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }


    class PiutangViewHolder extends RecyclerView.ViewHolder{

        private TextView tvItem1, tvItem2, tvItem3, tvItem4;
        private CardView cvContainer;

        private PiutangViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItem1 = itemView.findViewById(R.id.tv_item1);
            tvItem2 = itemView.findViewById(R.id.tv_item2);
            tvItem3 = itemView.findViewById(R.id.tv_item3);
            tvItem4 = itemView.findViewById(R.id.tv_item4);
            cvContainer = itemView.findViewById(R.id.cv_container);
        }
    }
}
