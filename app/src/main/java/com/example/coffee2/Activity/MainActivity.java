package com.example.coffee2.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.coffee2.Adapter.BestDrinkAdapter;
import com.example.coffee2.Adapter.CategoryAdapter;
import com.example.coffee2.Domain.Category;
import com.example.coffee2.Domain.Drinks;
import com.example.coffee2.R;
import com.example.coffee2.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());;
        setupNavigationDrawer();
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        ImageView filterBtn = findViewById(R.id.filterBtn);

        filterBtn.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        binding.viewAllTxt.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListDrinksActivity.class);
            intent.putExtra("isBestDrink", true);
            startActivity(intent);
        });

        initBestDrink();
        initCategory();
        setVariable();
        initBanner();
        getUserName();
    }

    private void setVariable() {
        binding.logoutBtn.setOnClickListener(v ->{
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
        });
        binding.searchBtn.setOnClickListener(v ->{
            String text = binding.searchEdt.getText().toString();
            if(!text.isEmpty()){
                Intent intent = new Intent(MainActivity.this,ListDrinksActivity.class);
                intent.putExtra("text",text);
                intent.putExtra("isSearch",true);
                startActivity(intent);
            }
        });
        binding.cartBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,CartActivity.class)));
    }

    private void initBanner() {
        DatabaseReference myRef = database.getReference("Banners");
        binding.progressBar2.setVisibility(View.VISIBLE);
        Query query = myRef.orderByChild("Active").equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        String imageUrl = issue.child("ImagePath").getValue(String.class);
                        if (imageUrl != null) {
                            Glide.with(MainActivity.this)
                                    .load(imageUrl)
                                    .transform(new RoundedCorners(18))
                                    .into(binding.bannerBtn);
                        }
                    }
                }
                binding.progressBar2.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar2.setVisibility(View.GONE);
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }

    private void getUserName() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (userId != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userName = snapshot.child("userName").getValue(String.class);
                    if (userName != null) {
                        TextView userNameTxt = findViewById(R.id.userNametxt);
                        userNameTxt.setText(userName);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Lỗi truy vấn dữ liệu", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initBestDrink(){
        DatabaseReference myRef = database.getReference("Drinks");
        binding.progressBarBestDrink.setVisibility(View.VISIBLE);
        ArrayList<Drinks> list=new ArrayList<>();
        Query query = myRef.orderByChild("BestDrink").equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot issue : snapshot.getChildren()){
                        Drinks drink = issue.getValue(Drinks.class);
                        if (drink != null && drink.isActive()) {
                            list.add(drink);
                        }
                    }
                    if(list.size()>0){
                        binding.bestDrinkView.setLayoutManager(
                                new GridLayoutManager(MainActivity.this, 2)
                        );
                        RecyclerView.Adapter adapter= new BestDrinkAdapter(list);
                        binding.bestDrinkView.setAdapter(adapter);
                    }
                    binding.progressBarBestDrink.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void initCategory(){
        DatabaseReference myRef = database.getReference("Category");
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        ArrayList<Category> list=new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot issue : snapshot.getChildren()){
                        list.add(issue.getValue(Category.class));
                    }
                    if(list.size()>0){
                        binding.categoryView.setLayoutManager(
                                new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false)
                        );
                        RecyclerView.Adapter adapter= new CategoryAdapter(list);
                        binding.categoryView.setAdapter(adapter);
                    }
                    binding.progressBarCategory.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setupNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.navigationView);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_settings) {
                // Mở EditProfileActivity
                Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Log.d("NavigationDrawer", "UserId: " + userId);
                intent.putExtra("UserId", userId);
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            } else if (id == R.id.nav_resetPass) {
                Intent intent = new Intent(MainActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }else if (id == R.id.nav_order_tracking) {
                Intent intent = new Intent(MainActivity.this, OrderTrackingActivity.class);
                startActivity(intent);
            }
            DrawerLayout drawer = findViewById(R.id.drawerLayout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
    }

}

