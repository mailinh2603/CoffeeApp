package com.example.coffee2.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee2.Adapter.DrinkListAdapter;
import com.example.coffee2.Domain.Drinks;
import com.example.coffee2.R;
import com.example.coffee2.databinding.ActivityListDrinksBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListDrinksActivity extends BaseActivity {
    ActivityListDrinksBinding binding;
    private RecyclerView.Adapter adapterListDrink;
    private int categoryId;
    private String categoryName;
    private String searchText;
    private boolean isSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityListDrinksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        initList();

    }

    private void initList(){
        DatabaseReference myRef = database.getReference("Drinks");
        binding.progressBar.setVisibility(View.VISIBLE);
        ArrayList<Drinks> list = new ArrayList<>();

        Query query;
        if (isSearch) {
            if (searchText == null) searchText = "";
            query = myRef.orderByChild("Title").startAt(searchText).endAt(searchText + '\uf8ff');
        } else {
            query = myRef.orderByChild("CategoryId").equalTo(categoryId);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Drinks drink = issue.getValue(Drinks.class);
                        if (drink != null) list.add(drink);
                    }
                    if (!list.isEmpty()) {
                        binding.drinkListView.setLayoutManager(new GridLayoutManager(ListDrinksActivity.this, 2));
                        adapterListDrink = new DrinkListAdapter(list);
                        binding.drinkListView.setAdapter(adapterListDrink);
                    }
                }
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void getIntentExtra(){
        categoryId=getIntent().getIntExtra("CategoryId",0);
        categoryName=getIntent().getStringExtra("CategoryName");
        searchText=getIntent().getStringExtra("text");
        isSearch=getIntent().getBooleanExtra("isSearch",false);

        binding.titleTxt.setText(categoryName);
        binding.backBtn.setOnClickListener(v -> finish());
    }
}