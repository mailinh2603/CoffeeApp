package com.example.coffee2.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.coffee2.Domain.BillDetailItem;
import com.example.coffee2.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderDetailsAdapter extends RecyclerView.Adapter<OrderDetailsAdapter.OrderViewHolder> {

    private List<BillDetailItem> list;

    public OrderDetailsAdapter(List<BillDetailItem> list) {
        this.list = list;
    }
    public static String formatCurrencyVND(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        BillDetailItem item = list.get(position);
        holder.tvName.setText(item.name);
        holder.tvOption.setText(item.option);
        holder.tvPrice.setText("Giá: " + formatCurrencyVND(item.price));
        holder.tvQuantity.setText("Số lượng: " + item.quantity);

        Glide.with(holder.itemView.getContext())
                .load(item.imagePath)
                .into(holder.imgDrink);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDrink;
        TextView tvName, tvOption, tvPrice, tvQuantity;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDrink = itemView.findViewById(R.id.imgDrink);
            tvName = itemView.findViewById(R.id.tvDrinkName);
            tvOption = itemView.findViewById(R.id.tvOption);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }
}
