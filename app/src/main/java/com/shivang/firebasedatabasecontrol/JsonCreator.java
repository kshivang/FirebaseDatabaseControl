package com.shivang.firebasedatabasecontrol;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import static com.android.volley.Request.Method.PATCH;
import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;

/**
 * Created by kshivang on 21/10/16.
 *
 */

class JsonCreator {

    static final int SAVE = 10;
    private static final String TAG = "Database";

    private Context mContext;
    private JsonCreator(Context context) {
        mContext = context;
    }

    static JsonCreator onCreate(Context context) {
        return new JsonCreator(context);
    }

    private final static int FLAG_STRING = 0, FLAG_INT = 1,
            FLAG_BOOLEAN = 2;


    private static void onDatabaseUpdate(JSONObject object, int method,
                                         final Context mContext) {
        String url = ((DatabaseActivity) mContext).getCurrentURL();
        if (((DatabaseActivity) mContext).isBaseUrl()) {
            url = url + "/.json";
        } else {
            url = url + ".json";
        }

        onProgress(mContext);
        JsonObjectRequest request = new JsonObjectRequest(method, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        Toast.makeText(mContext, "Value changed!", Toast.LENGTH_SHORT).show();
                        ((DatabaseActivity)mContext).onRefresh();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(mContext, "" + error.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Priority getPriority() {
                return Priority.HIGH;
            }
        };
        request.setShouldCache(false);
        AppController.getInstance(mContext).addToRequestQueue(request, TAG);
    }

    static void onValueDelete(@Nullable String key, final Context context) {

        String url;
        if (key != null) {
            url = ((DatabaseActivity) context).getCurrentURL() + "/" + key + ".json";
        } else {
            url = ((DatabaseActivity) context).getCurrentURL();
            if (((DatabaseActivity) context).isBaseUrl()) {
                url = url + "/.json";
            } else {
                url = url + ".json";
            }
        }


        onProgress(context);
        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Value changed!", Toast.LENGTH_SHORT).show();
                        ((DatabaseActivity)context).onRefresh();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(context, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                ((DatabaseActivity)context).onRefresh();
            }
        }) {
            @Override
            public Priority getPriority() {
                return Priority.HIGH;
            }
        };
        request.setShouldCache(false);
        AppController.getInstance(context).addToRequestQueue(request, TAG);

    }

    void onAdd(final int method) {
        new AlertDialog.Builder(mContext)
                .setMessage("Import json or create new database node")
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onNodeAdd(new NodeModel(), method, mContext);
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Import", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onJsonFileSelect(method);
                    }
                }).create().show();
    }



    static void onNodeAdd(@NonNull final NodeModel currentNode,
                                  final int method, final Context mContext) {

        ArrayList<String> keys = currentNode.getKeys();


        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface alertDialog, int which) {
                        alertDialog.dismiss();
                        new AlertDialog.Builder(mContext)
                                .setMessage("Are sure you want cancel," +
                                        " your current buffer would be lost?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        onNodeAdd(currentNode, method, mContext);
                                    }
                                }).create().show();
                    }
                });
        if (currentNode.getParent() == null) {
            String methodStr;
            switch (method) {
                case PATCH:
                    methodStr = "Patch";
                    break;
                case POST:
                    methodStr = "Post";
                    break;
                case PUT:
                    methodStr = "Put";
                    break;
                default:
                    methodStr = "Save";
            }
            builder.setTitle("New Node")
                    .setPositiveButton(methodStr, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (currentNode.getKeys() != null && currentNode.getKeys().size() > 0) {
                                if (method != SAVE)
                                    onDatabaseUpdate(onParse(currentNode, null), method, mContext);
                                else exportFile(onParse(currentNode, null).toString(), mContext);
                            } else {
                                Toast.makeText(mContext, "You have not created any node yet",
                                        Toast.LENGTH_SHORT).show();
                                onNodeAdd(currentNode, method, mContext);
                            }
                        }
                    });
        } else {
            builder.setTitle("Parent key : " + currentNode.getParent().getKey())
                    .setPositiveButton("Parent", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (currentNode.getKeys() != null && currentNode.getKeys().size() > 0)
                                currentNode.getParent().setValues(currentNode);
                            else {
                                ArrayList<String> keys = currentNode.getParent().getKeys();
                                String key = currentNode.getParent().getKey();
                                keys.remove(keys.lastIndexOf(key));
                                currentNode.getParent().setKey(null);
                                currentNode.getParent().setKeys(keys);
                            }
                            onNodeAdd(currentNode.getParent(), method, mContext);
                        }
                    });
        }

        View dialogView = View.inflate(mContext, R.layout.dialog_new_node, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        final EditText input = (EditText) dialogView.findViewById(R.id.et_key);
        RecyclerView lvNodes = (RecyclerView) dialogView.findViewById(R.id.list);
        if (keys != null && keys.size() > 0) {
//            ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
//                    android.R.layout.simple_list_item_1, keys);
            DatabaseRecyclerAdapter adapter = DatabaseRecyclerAdapter
                    .newInstance(currentNode, mContext,
                            new DatabaseRecyclerAdapter.Callback() {
                        @Override
                        void onKeysDelete(ArrayList<String> updatedKeys) {
                            currentNode.setKeys(updatedKeys);
                            alertDialog.dismiss();
                            onNodeAdd(currentNode, method, mContext);
                        }

                        @Override
                        void onClickItem(String key) {
                            alertDialog.dismiss();
                            onNodeAdd((NodeModel) currentNode.child(key),
                                    method, mContext);
                        }

                        @Override
                        void onEditValue(NodeModel currentNode, Class mClass) {
                            alertDialog.dismiss();
                            if (mClass == boolean.class || mClass == Boolean.class) {
                                onValueType(currentNode, FLAG_BOOLEAN, mContext, PATCH);
                            } else if (mClass == int.class || mClass == Integer.class) {
                                onValueType(currentNode, FLAG_INT, mContext, PATCH);
                            } else {
                                onValueType(currentNode, FLAG_STRING, mContext, PATCH);
                            }
                        }
                    });
            lvNodes.setLayoutManager(new LinearLayoutManager(mContext));

            lvNodes.setAdapter(adapter);
        } else lvNodes.setVisibility(View.GONE);


        dialogView.findViewById(R.id.bt_value).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> keys = currentNode.getKeys();

                if (TextUtils.isEmpty(input.getText())) {
                    Toast.makeText(mContext, "Write some key value!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (keys == null) {
                        keys = new ArrayList<>();
                    }

                    String key = input.getText().toString().replaceAll(" ", "_");
                    keys.add(key);

                    currentNode.setKey(key);
                    currentNode.setKeys(keys);

                    alertDialog.dismiss();
                    onValueType(currentNode, 0, mContext, method);
                }
            }
        });

        dialogView.findViewById(R.id.bt_sub_node).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> keys = currentNode.getKeys();
                if (TextUtils.isEmpty(input.getText())) {
                    Toast.makeText(mContext, "Write some key value!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (keys == null) {
                        keys = new ArrayList<>();
                    }

                    String key = input.getText().toString().replaceAll(" ", "_");
                    keys.add(key);

                    currentNode.setKey(key);
                    currentNode.setKeys(keys);

                    NodeModel subNode = new NodeModel();
                    subNode.setParent(currentNode);

                    alertDialog.dismiss();
                    onNodeAdd(subNode, method, mContext);
                }
            }
        });

        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
    }

    private void onJsonFileSelect(final int method) {
        final ArrayList<String> filesNames = fileNames();
        View dialogView = View.inflate(mContext,
                R.layout.dialog_select_file, null);
        ListView list = (ListView) dialogView.findViewById(R.id.list);
        if (filesNames != null && filesNames.size() > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                    android.R.layout.simple_list_item_1, filesNames);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent,
                                        View view, int position, long id) {
                    String path = mContext.getFilesDir().getAbsolutePath() +"/" +
                            filesNames.get(position);
                    try {
                        File file = new File(path);
                        FileInputStream stream = new FileInputStream(file);
                        String jsonStr = null;
                        try {
                            FileChannel fc = stream.getChannel();
                            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY,
                                    0, fc.size());
                            jsonStr = Charset.defaultCharset().decode(bb).toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                stream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (jsonStr != null) {
                            JSONObject jsonObject = new JSONObject(jsonStr);
                            onDatabaseUpdate(jsonObject, method, mContext);
                        } else {
                            Toast.makeText(mContext, "Empty file", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            new AlertDialog.Builder(mContext)
                    .setTitle("Choose File")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setView(dialogView).create().show();
        } else {
            Toast.makeText(mContext, "You don't have any instance of database saved",
                    Toast.LENGTH_SHORT).show();
        }


    }

    private static int type;
    static void onValueType(final NodeModel currentNode, int identifiedType,
                            final Context mContext, final int method) {
        if (!(identifiedType == -1)) {
            type = identifiedType;
        }

        new AlertDialog.Builder(mContext)
                .setTitle("Choose value Type")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setSingleChoiceItems(R.array.value_type, type, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        type = which;
                    }
                })
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onValueChange(currentNode, type, mContext, method);
                    }
                }).create().show();
    }

    private static void onValueChange(final NodeModel currentNode, int identifiedType,
                                      final Context mContext, final int method) {
        final EditText input = new EditText(mContext);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(5, 5, 5, 5);
        input.setLayoutParams(params);
        input.setGravity(Gravity.CENTER_HORIZONTAL);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setTitle("Choose value");
        switch (identifiedType) {
            case FLAG_STRING:
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(input.getText()))
                            Toast.makeText(mContext, "Empty string can't be set",
                                    Toast.LENGTH_SHORT).show();
                        else {
                            currentNode.setValues(input.getText().toString());
                        }
                        onNodeAdd(currentNode, method, mContext);
                    }
                });
                break;
            case FLAG_INT:
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputStr;
                        if (TextUtils.isEmpty(input.getText())) {
                            Toast.makeText(mContext, "Empty integer can't be set",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            inputStr = input.getText().toString();
                        }
                        if (TextUtils.isDigitsOnly(inputStr) || ((inputStr.contains("-") ||
                                inputStr.contains("+")) &&
                                TextUtils.isDigitsOnly(inputStr.substring(1)))) {
                            int intValue;
                            if (inputStr.contains("-")) {
                                intValue = -1 * Integer.valueOf(inputStr.substring(1));
                            } else if (inputStr.contains("+")) {
                                intValue = Integer.valueOf(inputStr.substring(1));
                            } else {
                                intValue = Integer.valueOf(inputStr);
                            }

                            currentNode.setValues(intValue);
                            onNodeAdd(currentNode, method, mContext);
                        }
                    }
                });
                break;
            case FLAG_BOOLEAN:
                builder.setSingleChoiceItems(R.array.bool_value,
                        -1, new DialogInterface
                                .OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    currentNode.setValues(true);
                                } else {
                                    currentNode.setValues(false);
                                }
                                onNodeAdd(currentNode, method, mContext);
                                dialog.dismiss();
                            }
                        });
                break;
        }

        AlertDialog alertDialog = builder.create();
        if (identifiedType == FLAG_STRING || identifiedType == FLAG_INT) {
            if (alertDialog.getWindow() != null ) {
                alertDialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        }
        alertDialog.show();
    }

    private static JSONObject onParse(NodeModel currentNode, JSONObject root) {

        try {
            if (root == null) {
                root = new JSONObject();
            }
            ArrayList<String> keys = currentNode.getKeys();
            if (keys != null) {
                for (String key : keys) {
                    if (currentNode.child(key).getClass() == NodeModel.class) {
                        root.put(key, onParse((NodeModel) currentNode.child(key), null));
                    } else {
                        root.put(key, currentNode.child(key));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return root;
    }

    ArrayList<String> fileNames() {
        FilenameFilter jsonFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json") || name.endsWith(".JSON");
            }
        };

        File root = mContext.getFilesDir();
        if (root != null)
            return new ArrayList<>(Arrays.asList(root.list(jsonFilter)));
        return null;
    }

    static void exportFile(final String jsonStr, final Context mContext) {
        View dialogView = View.inflate(mContext, R.layout.dialog_file_name, null);
        final EditText etName = (EditText) dialogView.findViewById(R.id.et_file_name);
        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setTitle("Choose name")
                .setView(dialogView)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(etName.getText())) {
                            mCreateAndSaveFile(((DatabaseActivity) mContext)
                                            .getCurrentURL()
                                            .replace("https://", "")
                                            .replaceAll("\\.", "_") + ".json",
                                            jsonStr, mContext);
                        } else {
                            mCreateAndSaveFile(etName.getText() + ".json",
                                    jsonStr, mContext);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
    }


    private static void mCreateAndSaveFile(String params, String mJsonResponse, Context mContext) {
        try {
            FileOutputStream outputStream = mContext.openFileOutput(params,
                    Context.MODE_PRIVATE);
            outputStream.write(mJsonResponse.getBytes());
            outputStream.close();
            Toast.makeText(mContext, "File Created", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ProgressDialog progressDialog;

    private static void onProgress(Context mContext) {
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Changing value");
        progressDialog.show();
    }
}
