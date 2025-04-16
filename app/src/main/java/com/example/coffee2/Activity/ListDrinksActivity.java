package com.example.coffee2.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee2.Adapter.DrinkListAdapter;
import com.example.coffee2.Domain.Drinks;
import com.example.coffee2.databinding.ActivityListDrinksBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListDrinksActivity extends BaseActivity {

    private ActivityListDrinksBinding binding;
    private RecyclerView.Adapter adapterListDrink;

    private int categoryId;
    private String categoryName;
    private String searchText;
    private boolean isSearch;
    private boolean isBestDrink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListDrinksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        initList();
    }

    private void getIntentExtra() {
        categoryId = getIntent().getIntExtra("CategoryId", 0);
        categoryName = getIntent().getStringExtra("CategoryName");
        searchText = getIntent().getStringExtra("text");
        isSearch = getIntent().getBooleanExtra("isSearch", false);
        isBestDrink = getIntent().getBooleanExtra("isBestDrink", false);

        if (categoryName != null) {
            binding.titleTxt.setText(categoryName);
        } else if (isBestDrink) {
            binding.titleTxt.setText("Best Drinks");
        } else if (isSearch) {
            binding.titleTxt.setText("Kết quả tìm kiếm");
        }

        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void initList() {
        DatabaseReference myRef = database.getReference("Drinks");
        binding.progressBar.setVisibility(View.VISIBLE);
        ArrayList<Drinks> list = new ArrayList<>();

        Query query;

        if (isSearch) {
            query = myRef.orderByChild("Title")
                    .startAt(searchText)
                    .endAt(searchText + '\uf8ff');
        } else if (isBestDrink) {
            query = myRef.orderByChild("BestDrink")
                    .equalTo(true);
        } else {
            query = myRef.orderByChild("CategoryId")
                    .equalTo(categoryId);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot item : snapshot.getChildren()) {
                        Drinks drink = item.getValue(Drinks.class);
                        if (drink != null) {
                            list.add(drink);
                        }
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
}
