package com.example.coffee2.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee2.Adapter.CartAdapter;
import com.example.coffee2.Helper.ChangeNumberItemsListener;
import com.example.coffee2.Helper.ManagmentCart;
import com.example.coffee2.R;
import com.example.coffee2.databinding.ActivityCartBinding;

public class CartActivity extends BaseActivity {
    private ActivityCartBinding binding;
    private RecyclerView.Adapter adapter;
    private ManagmentCart managmentCart;
    private double tax;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding = ActivityCartBinding.inflate(getLayoutInflater());
       setContentView(binding.getRoot());
       managmentCart = new ManagmentCart(this);
       setVariable();
       calculateCart();
       initList();
    }

    private void initList() {
        if(managmentCart.getListCart().isEmpty()){
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollviewCart.setVisibility(View.GONE);
        }
        else{
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollviewCart.setVisibility(View.VISIBLE);
        }

        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        binding.cartView.setLayoutManager(linearLayoutManager);
        adapter = new CartAdapter(managmentCart.getListCart(), this, () -> calculateCart());
        binding.cartView.setAdapter(adapter);
    }

    private void calculateCart(){
        double percenTax =0.02;
        double delivery = 10;
        tax= Math.round(managmentCart.getTotalFee()*percenTax*100.0)/100;
        double total = Math.round((managmentCart.getTotalFee()+tax+delivery)*100)/100;
        double itemTotal = Math.round(managmentCart.getTotalFee()*100)/100;

        binding.totalFeeTxt.setText(itemTotal + " đ");
        binding.taxTxt.setText(tax +" đ");
        binding.deliveryTxt.setText(delivery + " đ");
        binding.totalTxt.setText(total + " đ");
    }
    private void setVariable(){
        binding.backBtn.setOnClickListener(v ->finish());
    }
}