package com.eduard.flickrsearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class FlickrAdapter extends BaseAdapter {
  private Context context;
  private FlickrImage[] FlickrAdapterImage;

  FlickrAdapter(Context c, FlickrImage[] fImage) {
    context = c;
    FlickrAdapterImage = fImage;
  }

  public int getCount() {
    return FlickrAdapterImage.length;
  }

  public Object getItem(int position) {
    return FlickrAdapterImage[position];
  }

  public long getItemId(int position) {
    return position;
  }

  public View getView(int position, View convertView, ViewGroup parent) {
    ImageView image;
    TextView text;
    if (convertView == null) {
      convertView = LayoutInflater.from(context).inflate(R.layout.list_one, parent, false);

    }
    image = (ImageView) convertView.findViewById(R.id.imageView);
    text = (TextView) convertView.findViewById(R.id.pic_title);
    text.setText(FlickrAdapterImage[position].Title);
    image.setImageBitmap(FlickrAdapterImage[position].getBitmap());
    System.out.println(position);
    return convertView;
  }
  

}