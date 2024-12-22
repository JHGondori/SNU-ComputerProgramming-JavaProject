package com.example.stocksimulation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.stocksimulation.R;
import com.example.stocksimulation.model.UserRanking;

import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    private List<UserRanking> rankingList;

    public RankingAdapter(List<UserRanking> rankingList) {
        this.rankingList = rankingList;
    }

    @Override
    public RankingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RankingViewHolder holder, int position) {
        UserRanking userRanking = rankingList.get(position);
        holder.rankTextView.setText((position + 1) + "위");
        holder.nicknameTextView.setText(userRanking.getNickname());
        holder.returnRateTextView.setText(String.format("%.2f", userRanking.getReturnRate()) + "%");
    }

    @Override
    public int getItemCount() {
        return rankingList.size();
    }

    public void updateRankingList(List<UserRanking> newRankingList) {
        rankingList = newRankingList;
        notifyDataSetChanged();
    }

    public static class RankingViewHolder extends RecyclerView.ViewHolder {
        public TextView rankTextView;
        public TextView nicknameTextView;
        public TextView returnRateTextView;

        public RankingViewHolder(View itemView) {
            super(itemView);
            rankTextView = itemView.findViewById(R.id.rankTextView);
            nicknameTextView = itemView.findViewById(R.id.nicknameTextView);
            returnRateTextView = itemView.findViewById(R.id.returnRateTextView);
        }
    }
}
