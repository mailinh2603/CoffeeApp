package com.example.coffee2.Helper;

import android.content.Context;
import android.widget.Toast;


import com.example.coffee2.Domain.Drinks;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;


public class ManagmentCart {
    private Context context;
    private com.example.coffee2.Helper.TinyDB tinyDB;

    public ManagmentCart(Context context) {
        this.context = context;
        this.tinyDB=new com.example.coffee2.Helper.TinyDB(context);
    }

    public void insertFood(Drinks item) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ArrayList<Drinks> listpop = tinyDB.getListObject("CartList_" + userId);
        if (listpop == null) listpop = new ArrayList<>();

        boolean existAlready = false;
        int n = 0;

        for (int i = 0; i < listpop.size(); i++) {
            Drinks current = listpop.get(i);
            if (current.getTitle().equals(item.getTitle()) &&
                    ((current.getSugarOption() == null && item.getSugarOption() == null) ||
                            (current.getSugarOption() != null && current.getSugarOption().equals(item.getSugarOption()))) &&
                    ((current.getIceOption() == null && item.getIceOption() == null) ||
                            (current.getIceOption() != null && current.getIceOption().equals(item.getIceOption())))) {
                existAlready = true;
                n = i;
                break;
            }
        }

        if (existAlready) {
            listpop.get(n).setNumberInCart(listpop.get(n).getNumberInCart() + item.getNumberInCart());
        } else {
            listpop.add(item);
        }

        tinyDB.putListObject("CartList_" + userId, listpop);
        Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    public ArrayList<Drinks> getListCart() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ArrayList<Drinks> cartList = tinyDB.getListObject("CartList_" + userId);
        return cartList == null ? new ArrayList<>() : cartList;
    }

    public Double getTotalFee(){
        ArrayList<Drinks> listItem=getListCart();
        double fee=0;
        for (int i = 0; i < listItem.size(); i++) {
            fee=fee+(listItem.get(i).getPrice()*listItem.get(i).getNumberInCart());
        }
        return fee;
    }
    public void minusNumberItem(ArrayList<Drinks> listItem, int position, ChangeNumberItemsListener changeNumberItemsListener){
        if (listItem.get(position).getNumberInCart() == 1) {
            listItem.remove(position);
        } else {
            listItem.get(position).setNumberInCart(listItem.get(position).getNumberInCart() - 1);
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tinyDB.putListObject("CartList_" + userId, listItem);
        changeNumberItemsListener.change();
    }

    public void plusNumberItem(ArrayList<Drinks> listItem, int position, ChangeNumberItemsListener changeNumberItemsListener){
        listItem.get(position).setNumberInCart(listItem.get(position).getNumberInCart() + 1);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tinyDB.putListObject("CartList_" + userId, listItem);
        changeNumberItemsListener.change();
    }

    public void clearCart() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tinyDB.remove("CartList_" + userId);
    }

}
