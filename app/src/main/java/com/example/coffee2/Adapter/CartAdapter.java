package com.example.coffee2.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.coffee2.Domain.Drinks;
import com.example.coffee2.Helper.ChangeNumberItemsListener;
import com.example.coffee2.Helper.ManagmentCart;
import com.example.coffee2.R;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.viewholder> {
   ArrayList<Drinks> list;
   private ManagmentCart managmentCart;
   ChangeNumberItemsListener changeNumberItemsListener;

    public CartAdapter(ArrayList<Drinks> list, Context context, ChangeNumberItemsListener changeNumberItemsListener) {
        this.list = list;
        managmentCart = new ManagmentCart(context);
        this.changeNumberItemsListener = changeNumberItemsListener;
    }

    @NonNull
    @Override
    public CartAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_cart,parent,false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.viewholder holder, int position) {
        Drinks currentDrink = list.get(position);

        holder.title.setText(currentDrink.getTitle());
        holder.feeEachItem.setText((currentDrink.getNumberInCart() * currentDrink.getPrice()) + " đ");
        holder.totalEachItem.setText(currentDrink.getNumberInCart() + " * đ" + currentDrink.getPrice());
        holder.num.setText(String.valueOf(currentDrink.getNumberInCart()));

        // Hiển thị thông tin Đường và Đá
        holder.sugarOption.setText("Đường: " + currentDrink.getSugarOption());
        holder.iceOption.setText("Đá: " + currentDrink.getIceOption());

        Glide.with(holder.itemView.getContext())
                .load(currentDrink.getImagePath())
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(holder.pic);

        // Tăng giảm số lượng
        holder.plusItem.setOnClickListener(v -> managmentCart.plusNumberItem(list, position, () -> {
            notifyDataSetChanged();
            changeNumberItemsListener.change();
        }));

        holder.minusItem.setOnClickListener(v -> managmentCart.minusNumberItem(list, position, () -> {
            notifyDataSetChanged();
            changeNumberItemsListener.change();
        }));
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView title,feeEachItem, plusItem, minusItem,iceOption,sugarOption;
        ImageView pic;
        TextView totalEachItem, num;
        public viewholder(@NonNull View itemView){
            super(itemView);

            title = itemView.findViewById(R.id.titleTxt);
            pic = itemView.findViewById(R.id.pic);
            feeEachItem = itemView.findViewById(R.id.feeEachItem);
            plusItem=itemView.findViewById(R.id.plusCartBtn);
            minusItem=itemView.findViewById(R.id.minusCartBtn);
            totalEachItem=itemView.findViewById(R.id.totalEachItem);
            num=itemView.findViewById(R.id.numberItemTxt);
            sugarOption=itemView.findViewById(R.id.sugarOption);
            iceOption=itemView.findViewById(R.id.iceOption);
        }
    }
}
