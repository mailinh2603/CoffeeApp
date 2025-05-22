package com.example.coffee2.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee2.Domain.Bill;
import com.example.coffee2.R;

import java.util.List;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {
    private List<Bill> billList;
    private OnBillClickListener listener;

    public interface OnBillClickListener {
        void onBillClick(Bill bill);
    }

    public BillAdapter(List<Bill> billList, OnBillClickListener listener) {
        this.billList = billList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill bill = billList.get(position);
        holder.tvBillId.setText("Mã đơn: " + bill.getBillId());
        holder.tvOrderDate.setText("Ngày đặt: " + bill.getOrderDate());
        holder.tvStatus.setText("Trạng thái: " + bill.getOrderStatus());

        holder.itemView.setOnClickListener(v -> listener.onBillClick(bill));
    }

    @Override
    public int getItemCount() {
        return billList.size();
    }

    static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView tvBillId, tvOrderDate, tvStatus;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBillId = itemView.findViewById(R.id.tvItemBillId);
            tvOrderDate = itemView.findViewById(R.id.tvItemOrderDate);
            tvStatus = itemView.findViewById(R.id.tvItemStatus);
        }
    }
}
