package edu.pl.lodz.p.course.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.R.anim;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileChooserActivity extends Activity {
	
	
	public final static int GET_FILE_PATH = 0x01;

	protected final String mRoot = "/";
	protected final String mParent = "..";
	protected List<String> mCurrentPaths = null;

	protected TextView mCurrentPwd;
	protected ArrayAdapter<String> mListViewAdapter;

	protected FileChooserActivity mThis;
	protected Dialog mBookmarks;
	protected BookmarkAdapter mBookmarksAdapter;
	
	
	protected class BookmarkAdapter extends ArrayAdapter<String> {
		
		List<String> data;
		
		public BookmarkAdapter(Context context, int textViewResourceId,List<String> objects) {
			super(context, textViewResourceId, objects);
			data = objects;
		}


		class BookmarkListHolder {
			 public ImageView rm;
		     public TextView name;
		     public TextView path;
		}
		
		class DeleteClick implements OnClickListener {
			
			BookmarkListHolder parent;
			
			public DeleteClick(BookmarkListHolder parentHolder) {
				this.parent = parentHolder;
			}

			@Override
			public void onClick(View v) {

				String removeBookmark = "" + parent.name.getText();
				
				data.remove(removeBookmark);
				
				try {
					FileInputStream fis = mThis.getApplication().openFileInput("bookmarks");
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));

					List<String> bookmarks = new ArrayList<String>();

					String line = "";
					while ((line = br.readLine()) != null) {
						if (!line.contains(removeBookmark))
							bookmarks.add(line);
					}
					fis.close();
					
					FileOutputStream fos = mThis.getApplication().openFileOutput("bookmarks",Context.MODE_PRIVATE);
					PrintStream printStream = new PrintStream(fos);
					for (String s : bookmarks)
						printStream.print(s);
					
					printStream.close();

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			
				bookmarksRefresh();
				
			}
			
		};
	
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			   View row = convertView;
			   BookmarkListHolder holder = null;
		       
		        if(row == null) {
		            LayoutInflater inflater = getLayoutInflater();
		            row = inflater.inflate(R.layout.bookmarks_item, parent, false);
		            
		            holder = new BookmarkListHolder();
		            
		            holder.rm = (ImageButton)row.findViewById(R.id.bookmark_del);
		            holder.rm.setOnClickListener(new DeleteClick(holder));
		            holder.rm.setFocusable(false);
		            
		            holder.name = (TextView)row.findViewById(R.id.bookmark_name);
		            holder.path = (TextView)row.findViewById(R.id.bookmark_path);
		            
		            row.setTag(holder);
		            
		        }
		        else {
		            holder=(BookmarkListHolder)row.getTag();
		        }
		       
		        holder.name.setText(data.get(position).split(" ")[0]);
		        holder.path.setText(data.get(position).split(" ")[1]);
		       
		        return row;
		}
	};
	
	
	protected class FileArrayAdapter extends ArrayAdapter<String> {
		
		
		protected class Holder {
			 public ImageView typeImg;
		     public TextView fileName;
		     public TextView fileInfo;
		}

		
		public FileArrayAdapter(Context context, int textViewResourceId,List<String> objects) {
			super(context, textViewResourceId, objects);
		}

		
		@Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View row = convertView;
	        Holder holder = null;
	       
	        if(row == null) {
	            LayoutInflater inflater = mThis.getLayoutInflater();
	            row = inflater.inflate(R.layout.fileman_item, parent, false);
	            
	            holder = new Holder();
	            holder.typeImg = (ImageView)row.findViewById(R.id.imgIcon);
	            holder.fileName = (TextView)row.findViewById(R.id.txtTitle);
	            holder.fileInfo = (TextView)row.findViewById(R.id.txtInfo);
	           
	            row.setTag(holder);
	            
	        }
	        else {
	            holder=(Holder)row.getTag();
	        }
	       
	        holder.fileName.setText(mCurrentPaths.get(position));
	        holder.fileInfo.setText(mThis.getString(R.string.empty));
	        
	        
	        String clickedFilePath = mCurrentPwd.getText() + ( mCurrentPwd.getText().equals(mRoot)  ? "" : "/" ) + mCurrentPaths.get(position);
	        
	        File test = new File(clickedFilePath);
	        try {
	        	
	        	String info = "";
	
	        	    String type = "regular file";
	        	    
	        	    String extension = MimeTypeMap.getFileExtensionFromUrl(test.getAbsolutePath());
	        	    if (extension != null) {
	        	        MimeTypeMap mime = MimeTypeMap.getSingleton();
	        	        String t = mime.getMimeTypeFromExtension(extension);
	        	        type = t==null ? type : t ;
	        	    }


	        	info += mThis.getString(R.string.type)+":"+(test.isDirectory() ? "directory" : type)+", " + 
	        			mThis.getString(R.string.last_modified) + ": " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date(test.lastModified())) + "\n" + 
	        			mThis.getString(R.string.size) + ": " + fileSizeConvert(test.length()) + ", " +
	        			mThis.getString(R.string.permissions) + ": " + ( test.canRead() ? "r" : "_" )+( test.canWrite() ? "w" : "_" )+( test.canExecute() ? "x" : "_" );
	        			 
	        	holder.fileInfo.setText(info) ;
	        	 
	        } catch (Exception e) {}
	        
	        
	        if ( mCurrentPaths.get(position).equals(mParent)) {
	        	holder.typeImg.setImageResource(  R.drawable.cd );
	        } else if ( test.isDirectory() ) {
	        	holder.typeImg.setImageResource(  R.drawable.directory );
	        } else
	        	holder.typeImg.setImageResource( R.drawable.fs_regular );
	       
	        
	        return row;
	    }
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mThis = this;

		mCurrentPaths = new ArrayList<String>();

		mCurrentPaths.add(mParent);

		ListView lv;

		lv = (ListView) findViewById(R.id.filelist);
		mListViewAdapter = new FileArrayAdapter(this,R.layout.fileman_item, mCurrentPaths);

		View header = (View)getLayoutInflater().inflate(R.layout.fileman_header, null);
//        lv.addHeaderView(header);
		lv.setAdapter(mListViewAdapter);

		
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,int position, long id)  {
				
				
				final String clickedPath = mThis.mListViewAdapter.getItem(position);
				final String clickedFilePath = mCurrentPwd.getText() + (mCurrentPwd.getText().equals(mRoot) ? "" : "/") + clickedPath;
				File clicked = new File(clickedFilePath);
				
				if ( !clickedPath.endsWith(mParent) && clicked.isDirectory() ) {
				
				AlertDialog.Builder alert = new AlertDialog.Builder(mThis);

				alert.setTitle("Bookmark");
				alert.setMessage("enter name");
				final EditText input = new EditText(mThis);
				alert.setView(input);
				alert.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int whichButton) {
								String subtext = input.getText().toString();

								try {
									FileOutputStream fos = mThis.getApplication().openFileOutput("bookmarks",Context.MODE_APPEND);
									PrintStream printStream = new PrintStream(fos);
									printStream.print( input.getText().toString() + " " + clickedFilePath + "\n");
									
									//@TODO save bookmarks
									
									printStream.close();

								} catch (FileNotFoundException e) {e.printStackTrace();}

							}
						});

				alert.setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int whichButton) {/* Canceled.*/}
						});

				alert.show();
				} else 
					Toast.makeText(mThis, mThis.getString(R.string.bookmark_err), Toast.LENGTH_SHORT).show();
				
				return false;
			}

		});
		
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,int position, long id) {

				String clickedPath = mThis.mListViewAdapter.getItem(position);
				String clickedFilePath = mCurrentPwd.getText() + (mCurrentPwd.getText().equals(mRoot) ? "" : "/") + clickedPath;
				File clicked = new File(clickedFilePath);

				// go to parent directory
				if (clickedPath.equals(mParent)) {

					String currentPath = "" + mCurrentPwd.getText();
					String[] dirs = currentPath.split("/");

					String newPath = "/";
					for (int i = 0; i < dirs.length - 1; ++i)
						if (!dirs[i].equalsIgnoreCase(""))
							newPath += dirs[i] + "/";

					Log.i("", "size:" + dirs.length + " path " + currentPath
							+ " new " + newPath);

					switchDirectory(newPath);

				}// go to choosen directory
				else if (clicked.isDirectory()) {

					switchDirectory(clickedFilePath);

				}// open file, if appropriate handler is avaiable
				else if (clicked.isFile()) {

					try {
						if (clicked.canRead() ) {

							Intent i = new Intent(getApplicationContext(),TextPreview.class);
							i.putExtra("file", clicked);
							setResult(RESULT_OK, i);
							finish();
							
						} else 
							throw new Exception();
						
					} catch (Exception e) {

						Log.e(mThis.getClass().getSimpleName(),"NULL, AAAAAAA!!!!!!!!");

						Toast.makeText(mThis, mThis.getString(R.string.noacces), Toast.LENGTH_SHORT).show();
					}
				}

			}
		});
		
		mCurrentPwd = (TextView) findViewById(R.id.pwd);
		
		switchDirectory("/");

	}

	private void switchDirectory(String newDir) {
		
		
		Log.i("switchDirectory","in:"+newDir);

		try {
			File[] newPath = new File(newDir).listFiles();

			Log.i("", newDir + " ");
			if (newPath.length > 0) {
				newPath[0].getName();
			}
				int currentSize = mCurrentPaths.size();
				for (int i = currentSize - 1; i > 0; i--) 
					mCurrentPaths.remove(i);

				for (File f : newPath)
					mCurrentPaths.add(f.getName());
			
			mCurrentPwd.setText(newDir);
			Log.d("DBG","sort");
			Collections.sort(mCurrentPaths, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					
					if ( o1.equals(mParent))
						return -1;
					File of1 = new File(mCurrentPwd.getText() + (mCurrentPwd.getText().equals(mRoot) ? "" : "/") + o1);
					File of2 = new File(mCurrentPwd.getText() + (mCurrentPwd.getText().equals(mRoot) ? "" : "/") + o2);
					if (of1.isDirectory()) {
						if (of2.isDirectory())
							return o1.compareTo(o2);
						else return -1;
					} else {
						if (of2.isDirectory())
							return 1;
						else return o1.compareTo(o2);
					}
					
				}
			});
				
			mListViewAdapter.notifyDataSetChanged();

		} catch (Exception e) {

			Log.e(mThis.getClass().getSimpleName(), "NULL, AAAAAAA!!!!!!!!");

			Toast.makeText(mThis, mThis.getString(R.string.noacces),
					Toast.LENGTH_SHORT).show();
		}

	}
	
	private String fileSizeConvert(long size) {
		
		String[] multiplier = {"","k","M","G"};
		int i=0;
		while ( i<3) {
			if (size > 2048) {
				size/=1024;
			} else
				break;
			++i;
		}
		
		return size+multiplier[i]+"B";
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.settings:
			//@TODO
			break;
		case R.id.bookmark:
			createBookmarks();			
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void bookmarksRefresh() {
		mBookmarks.dismiss();
		mBookmarksAdapter.notifyDataSetChanged();
		createBookmarks();
	}
	
	protected void createBookmarks() {
		try {

			FileInputStream fis = mThis.getApplication().openFileInput("bookmarks");
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			
			final List<String> bookmarks = new ArrayList<String>();
			
			String line = "";
			while ( (line=br.readLine()) != null ) {
				bookmarks.add(line);
			}
			
			Log.i("I",bookmarks.toString());
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			mBookmarksAdapter =  new BookmarkAdapter(this, R.layout.bookmarks_item, bookmarks);
			builder.setAdapter(mBookmarksAdapter, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switchDirectory(bookmarks.get(which).split(" ")[1]);
					dialog.cancel();
				}
			});
			
			mBookmarks = builder.create();
			mBookmarks.show();
			
			fis.close();
			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Toast.makeText(mThis,getString(R.string.bookmarks)+" "+getString(R.string.empty_item), Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
