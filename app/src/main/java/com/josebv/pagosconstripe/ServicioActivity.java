package com.josebv.pagosconstripe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class ServicioActivity extends AppCompatActivity {

    private final String URI_BASE = "http://192.168.137.1:3000";

    ShapeableImageView ivImgServicio, ivImgPrestador;
    TextView tvTitulo, tvDescripcion, tvNombrePrestador, tvInfoPrestador, tvPrecio;
    String idServicio;
    RequestQueue requestQueue;
    String URI_SERVICIO = URI_BASE + "/api/servicio/";
    String URI_IMG_USERS = URI_BASE + "/static/images/users/";
    String URI_IMG_SERVICIOS = URI_BASE + "/static/images/services/";
    Button btnSolicitar;

    // Variable temporal que saldra de los Shared Preferences
    String idCliente = "63b3709de16477ad63f97345";

    // Stripe
    PaymentSheet paymentSheet;
    String paymentIntentClientSecret;
    PaymentSheet.CustomerConfiguration customerConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicio);

        requestQueue = Volley.newRequestQueue(this);

        // Obtener el ID del servicio a consultar
        idServicio = getIntent().getStringExtra("idServicio");

        ivImgServicio = findViewById(R.id.imgServicio);
        ivImgPrestador = findViewById(R.id.imgPrestador);
        tvTitulo = findViewById(R.id.tvTitulo);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvNombrePrestador = findViewById(R.id.tvNombrePrestador);
        tvInfoPrestador = findViewById(R.id.tvInfoPrestador);
        tvPrecio = findViewById(R.id.tvPrecio);
        btnSolicitar = findViewById(R.id.btnSolicitar);

        getServicio(idServicio);

        // Stripe
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        fetchApi(idCliente, idServicio);

        btnSolicitar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (paymentIntentClientSecret != null) {
                    presentPaymentSheet();
                }
            }
        });

        // Boton de retorno
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Boton de retorno
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Crea la configuración del consumidor
     */
    private void presentPaymentSheet() {
        final PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("Home Care Plus")
            .customer(customerConfig)
            // Set `allowsDelayedPaymentMethods` to true if your business can handle payment methods
            // that complete payment after a delay, like SEPA Debit and Sofort.
            .allowsDelayedPaymentMethods(true)
            .build();

        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);
    }

    /**
     * Verifica el estado del pago
     *
     * @param paymentSheetResult
     */
    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Pago cancelado", Toast.LENGTH_LONG).show();

        } else if(paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(this, ((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage(), Toast.LENGTH_LONG).show();

        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            fetchApi(idCliente, idServicio);
            Toast.makeText(this, "Pago completo", Toast.LENGTH_LONG).show();

            // Iniciar una nueva Activity de pago correcto
        }
    }

    /**
     * Realiza la petición a la API para hacer el pago
     *
     * @param idCliente ID del cliente que pagará el servicio (sale de los Shared Preferences)
     * @param idServicio ID sel servicio que será contratado
     */
    private void fetchApi(String idCliente, String idServicio) {
        String URI_PAGOS = URI_BASE + "/api/cliente/" + idCliente + "/pagar/" + idServicio;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URI_PAGOS,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject res = new JSONObject(response);

                        customerConfig = new PaymentSheet.CustomerConfiguration(
                            res.getString("customer"),
                            res.getString("ephemeralKey")
                        );

                        paymentIntentClientSecret = res.getString("paymentIntent");
                        PaymentConfiguration.init(getApplicationContext(), res.getString("publishableKey"));

                    } catch (JSONException e) { e.printStackTrace(); }

                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ServicioActivity.this, "Ha ocurrido un error", Toast.LENGTH_LONG).show();
                }
            }
        );

        requestQueue.add(stringRequest);

    }

    /**
     * Obtiene información del servicio desde la API
     *
     * @param idServicio ID del servicio a consultar
     */
    private void getServicio(String idServicio) {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URI_SERVICIO + idServicio,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject res = new JSONObject(response);
                            Integer status = Integer.parseInt( res.getString("code") );

                            if (status == 200) {
                                JSONObject servicio = new JSONObject( res.getString("servicio") );
                                JSONObject prestador = new JSONObject( servicio.getString("prestadorDeServicio") );

                                String titulo = servicio.getString("titulo");
                                String descripcion = servicio.getString("descripcion");
                                String imagen = servicio.getString("imagen");
                                String precio = servicio.getString("precio");
                                String nombrePrestador = prestador.getString("nombre");
                                String profesionPrestador = prestador.getString("profesion");
                                String imagenPrestador = prestador.getString("imagen");

                                insertarDatos(titulo, imagen, descripcion, precio, nombrePrestador, profesionPrestador, imagenPrestador);

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

    /**
     * Coloca los datos de la API en la vista
     *
     * @param titulo Título del servicio
     * @param imgServicio Nombre y formato de la imagen del servicio
     * @param descripcion Descripción del servicio
     * @param precio Precio del servicio
     * @param nombrePrestador Nombre del usuario prestador
     * @param profesionPrestador Profesión del usuario prestador
     * @param imgPrestador Nombre y formato de la imagen del usuario prestador
     */
    private void insertarDatos(String titulo, String imgServicio, String descripcion, String precio, String nombrePrestador, String profesionPrestador, String imgPrestador) {
        // Formatear numeros por miles
        DecimalFormat formato = new DecimalFormat("$###,###,###.##");
        Double precioServicio = Double.parseDouble(precio);

        tvTitulo.setText(titulo);
        tvDescripcion.setText(descripcion);
        tvPrecio.setText( formato.format(precioServicio) );
        tvNombrePrestador.setText(nombrePrestador);
        tvInfoPrestador.setText(profesionPrestador);

        // Cargar imagenes
        Picasso.get().load(URI_IMG_SERVICIOS + imgServicio).into(ivImgServicio);
        Picasso.get().load(URI_IMG_USERS + imgPrestador).into(ivImgPrestador);
    }
}