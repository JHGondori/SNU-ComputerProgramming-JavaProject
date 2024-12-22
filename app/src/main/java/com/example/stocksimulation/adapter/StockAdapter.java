package com.example.stocksimulation.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.stocksimulation.R;
import com.example.stocksimulation.activity.StockActivity;
import com.example.stocksimulation.model.Stock;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private List<Stock> stockList;

    public StockAdapter(List<Stock> stockList) {
        this.stockList = stockList;
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {
        Stock stock = stockList.get(position);
        holder.symbolTextView.setText(stock.getSymbol() + " - " + stock.getName());
        holder.countTextView.setText("개수: " + stock.getCount());
        holder.investedTextView.setText("투자 금액: " + String.format("%.2f", stock.getTotalInvested()) + " USD");
        holder.currentValueTextView.setText("현재 금액: " + String.format("%.2f", stock.getCurrentValue()) + " USD");
        holder.returnTextView.setText("수익률: " + String.format("%.2f", stock.getReturnRate()) + "%");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // StockActivity로 이동
                Context context = v.getContext();
                Intent intent = new Intent(context, StockActivity.class);
                intent.putExtra("STOCK_SYMBOL", stock.getSymbol()); // 주식 기호 전달
                intent.putExtra("STOCK_NAME", stock.getName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    // RecyclerView 업데이트를 위한 함수
    public void updateStockList(List<Stock> newStockList) {
        stockList = newStockList;
        notifyDataSetChanged();
    }

    public static class StockViewHolder extends RecyclerView.ViewHolder {
        public TextView symbolTextView;
        public TextView countTextView;
        public TextView investedTextView;
        public TextView currentValueTextView;
        public TextView returnTextView;

        public StockViewHolder(View itemView) {
            super(itemView);
            symbolTextView = itemView.findViewById(R.id.symbolTextView);
            countTextView = itemView.findViewById(R.id.countTextView);
            investedTextView = itemView.findViewById(R.id.investedTextView);
            currentValueTextView = itemView.findViewById(R.id.currentValueTextView);
            returnTextView = itemView.findViewById(R.id.returnTextView);
        }
    }
}
