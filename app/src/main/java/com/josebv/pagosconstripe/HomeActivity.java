package com.josebv.pagosconstripe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements RecyclerViewInterface {

    RecyclerView rvListaServiciosPub;
    ArrayList<Servicio> listaServicios;
    Adaptador adaptador;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        requestQueue = Volley.newRequestQueue(this);

        rvListaServiciosPub = findViewById(R.id.rvListaServiciosPub);
        rvListaServiciosPub.setLayoutManager(new LinearLayoutManager(this));
        rvListaServiciosPub.setHasFixedSize(true);

        listaServicios = new ArrayList<Servicio>();

        getServiciosPrestador();
    }

    /**
     * Consulta los servicios de un prestador
     */
    private void getServiciosPrestador() {
        // Consultar el ID
        String idUsuario = "63b372dce16477ad63f9735f";

        // URI de los servicios de X prestador
        String URI_SERVICIOS_PRESTADOR = "https://homecareplus.vercel.app/api/prestador/" + idUsuario + "/servicios";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URI_SERVICIOS_PRESTADOR,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject res = new JSONObject(response);
                            Integer status = Integer.parseInt( res.getString("code") );

                            if (status == 200) {
                                JSONArray datos = new JSONArray( res.getString("datos") );

                                for (int i = 0; i < datos.length(); i++) {
                                    JSONObject servicio = datos.getJSONObject(i);

                                    String id = servicio.getString("_id");
                                    String img = servicio.getString("imagen");
                                    String titulo = servicio.getString("titulo");
                                    String descripcion = servicio.getString("descripcion");
                                    String precio = servicio.getString("precio");

                                    listaServicios.add(new Servicio(id, img, titulo, descripcion, precio));
                                }

                                adaptador = new Adaptador(getApplicationContext(), listaServicios, HomeActivity.this);
                                rvListaServiciosPub.setAdapter(adaptador);

                                adaptador.notifyDataSetChanged();
                            }

                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                }
        );

        requestQueue.add(stringRequest);

    }

    // Abrir la activity de Detalles del servicio, enviandole el ID
    @Override
    public void onItemClick(int position) {
        String idServicio = listaServicios.get(position).getId();

        Intent abrirServicio = new Intent(this, ServicioActivity.class);
        abrirServicio.putExtra("idServicio", idServicio);

        startActivity(abrirServicio);
    }

}
