package rest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import Tablas.Ayuda;
import Tablas.Concentracion;
import Tablas.Placa;
import Tablas.Posicion;
import Tablas.Usuario;
import Tablas.Zona;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;



public class RestServer extends AbstractVerticle {

	private Gson gson;

	MySQLPool mySqlClient;

	MqttClient mqttClient;
	public void start(Promise<Void> startPromise) {
		// Creating some synthetic data
		//createSomeData(25);
		MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306).setHost("localhost")
				.setDatabase("covid_database").setUser("root").setPassword("iissi$root");

		//getOne(null);

		PoolOptions poolOptions = new PoolOptions().setMaxSize(30);
		gson = new Gson();
		mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);

		// Instantiating a Gson serialize object using specific date format
		//gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

		// Defining the router object
		Router router = Router.router(vertx);

		// Handling any server startup result
		vertx.createHttpServer().requestHandler(router::handle).listen(8080, result -> {
			if (result.succeeded()) {
				startPromise.complete();
			} else {
				startPromise.fail(result.cause());
			}
		});

		// Defining URI paths for each method in RESTful interface, including body
		// handling by /api/users* or /api/users/*
		router.route("/api/*").handler(BodyHandler.create());
		router.get("/api/placa/:modelo").handler(this::getOnePlaca);//funciona
		router.get("/api/concentracion/:idPlaca/:hours").handler(this::getOneConcentracion);//funciona
		router.get("/api/posicion/:idUsuario/:latitud").handler(this::getOnePosicion);//claudia //funciona
		router.post("/api/concentracion").handler(this::addOneConcentracion);//funciona
		router.post("/api/placas").handler(this::addOnePlaca); //claudia //funciona
		router.post("/api/usuario/login").handler(this::getUserLogged);//funciona
		router.post("/api/usuario/new").handler(this::postNewUser); //funciona
		router.post("/api/zona").handler(this::postNewZona); //funciona
		router.post("/api/posicion/new").handler(this::postNewPosicion);//funciona
		router.delete("/api/usuario/:idUsuario").handler(this::deleteUser);
		router.put("/api/usuario/update/:idUsuario").handler(this::putUser);
		
		//////////////////////////////////////////////////////////////////////////////////////
		
		
		mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
        mqttClient.connect(1883, "192.168.1.139", s -> { // cambiar ip a la del ordenador que haga de servidor

        	mqttClient.subscribe("topic_2", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
                if (handler.succeeded()) {
                    System.out.println("Suscripción " + mqttClient.clientId());
                }
            });

            mqttClient.publishHandler(handler -> {
                System.out.println("Mensaje recibido:");
                System.out.println("    Topic: " + handler.topicName().toString());
                System.out.println("    Id del mensaje: " + handler.messageId());
                System.out.println("    Contenido: " + handler.payload().toString());
                try {
                    Concentracion g = gson.fromJson(handler.payload().toString(), Concentracion.class);
                    System.out.println("    Concentración: " + g.toString());
                } catch (JsonSyntaxException e) {
                    System.out.println("    No es un Concentración. ");
                }
            });
            mqttClient.publish("topic_1", Buffer.buffer("Conexión"), MqttQoS.AT_LEAST_ONCE, false, false);

        });

        // getAll();
    }

	private void postNewZona(RoutingContext routingContext) {
		try{
			final Zona zona = gson.fromJson(routingContext.getBodyAsString(), Zona.class);
			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().preparedQuery("INSERT INTO zona (latitud, longitud) VALUES (?,?)")
							.execute(Tuple.of(zona.getLatitud(),zona.getLongitud()),
							res -> {
								if (res.succeeded()) {
									System.out.println("OK!");
									routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
									.end(gson.toJson(zona));
									}else {
										System.out.println("Error: " + res.cause().getLocalizedMessage());
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
										.end();
								}
							});
				} else {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
					.end();
				}
			});
			}catch (Exception e) {
				int na  = 0;
			}
	}
	
	
	
	private void getUserLogged(RoutingContext routingContext) {
		String email = routingContext.getBodyAsJson().getString("email");
		String password =routingContext.getBodyAsJson().getString("password");
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery("SELECT * FROM usuario WHERE email = ? AND password = ?")
						.execute(Tuple.of(email, password), res -> { 
							if (res.succeeded()) {
								// Get the result set
								RowSet<Row> resultSet = res.result();
								List<Usuario> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new Usuario(elem.getInteger("idUsuario"),elem.getString("nombre"), elem.getString("apellidos"),elem.getString("email"), elem.getString("dni"),
											elem.getLong("fechaNacimiento"), elem.getString("password")));
								}
								System.out.println(result.toString());
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
								.end(gson.toJson(result));
							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
								.end();
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
				.end();
			}
		});
	}
	
	private void postNewUser(RoutingContext routingContext) {
		try{
			final Usuario usuario = gson.fromJson(routingContext.getBodyAsString(), Usuario.class);
			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().preparedQuery("INSERT INTO usuario (nombre, apellidos, password, email, fechaNacimiento, dni) VALUES (?,?,?,?,?,?)")
							.execute(Tuple.of(usuario.getNombre(),usuario.getApellidos(),usuario.getPassword(),usuario.getEmail(), usuario.getFechaNacimiento(), usuario.getDni()),
							res -> {
								if (res.succeeded()) {
									System.out.println("OK!");
									routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
									.end(gson.toJson(usuario));
									}else {
										System.out.println("Error: " + res.cause().getLocalizedMessage());
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
										.end();
								}
							});
				} else {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
					.end();
				}
			});
			}catch (Exception e) {
				int na  = 0;
			}
	}
	
	private void putUser(RoutingContext routingContext) {
		try{
			final Usuario usuario = gson.fromJson(routingContext.getBodyAsString(), Usuario.class);
			String idUsuario = (routingContext.request().getParam("idUsuario"));
			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().preparedQuery("UPDATE usuario SET nombre = ?, apellidos = ?, password = ?, email = ?, fechaNacimiento = ?, dni = ? WHERE idUsuario = ?")
							.execute(Tuple.of(usuario.getNombre(),usuario.getApellidos(),usuario.getPassword(),usuario.getEmail(), usuario.getFechaNacimiento(), usuario.getDni(),idUsuario),
							res -> {
								if (res.succeeded()) {
									System.out.println("OK!");
									routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
									.end(gson.toJson(usuario));
									}else {
										System.out.println("Error: " + res.cause().getLocalizedMessage());
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
										.end();
								}
							});
				} else {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
					.end();
				}
			});
			}catch (Exception e) {
				int na  = 0;
			}
	}
	
	private void deleteUser(RoutingContext routingContext) {
		String idUsuario = (routingContext.request().getParam("idUsuario"));
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery("DELETE FROM usuario WHERE idUsuario = ?")
				.execute(Tuple.of(idUsuario), res -> {
							if (res.succeeded()) {
							
								routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
								.end("Usuario eliminado");
							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
								.end();
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
				.end();
			}
		});
	}
	
	//Funcion auxiliar para calcular distancias
	public static double distanciaCoord(double lat1, double lng1, double lat2, double lng2) {   
        double radioTierra = 6371;//en kilómetros  
        double dLat = Math.toRadians(lat2 - lat1);  
        double dLng = Math.toRadians(lng2 - lng1);  
        double sindLat = Math.sin(dLat / 2);  
        double sindLng = Math.sin(dLng / 2);  
        double va1 = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)  
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));  
        double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));  
        double distancia = radioTierra * va2;  
        return distancia;  
    }  
	
	//
	
	private void postNewPosicion(RoutingContext routingContext) {

		try{
			final Posicion posicion = gson.fromJson(routingContext.getBodyAsString(), Posicion.class);
			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().preparedQuery("SELECT latitud,longitud FROM zona ORDER BY idZona;").execute( new Handler<AsyncResult<RowSet<Row>>>() 
									{
										@Override
										public void handle(AsyncResult<RowSet<Row>> event) {
											List<Double> listaLat = new ArrayList<>();
											List<Double> listaLon = new ArrayList<>();
											for (Row row: event.result()) {
												listaLat.add(row.getDouble(0));
												listaLon.add(row.getDouble(1));
											}
											//System.out.println(listaLat); //[37.36365763196362, 38.046389104966444]
											//System.out.println(listaLon); //[-5.98885700247913, -4.172474233364769]
											//mySqlClient.preparedQuery("SELECT latitud,longitud FROM posicion where idUsuario = ? ORDER BY idPosicion DESC limit 1").execute(Tuple.of(posicion.getIdUsuario()), new Handler<AsyncResult<RowSet<Row>>>() {
												int idZona = 0;
												//@Override
												//public void handle(AsyncResult<RowSet<Row>> event) {
													Double lat = posicion.getLatitud();
													Double lon = posicion.getLongitud();
													/*
													for (Row row: event.result()) {
														lat=row.getDouble(0);
														lon=row.getDouble(1);
													}
													*/
													//System.out.println(listaLat.size());
													//System.out.println(lat);
													//System.out.println(lon);
													Double distancia = 61523068.1;
													for(int i=0; i<listaLat.size();i++) {
														Double dist = distanciaCoord(listaLat.get(i), listaLon.get(i),lat,lon);
														System.out.println(dist);
														if(dist < distancia) {
															distancia = dist;
															idZona = i+1;
														}
													}
													distancia = 61523068.1;
													
													mySqlClient.preparedQuery("INSERT INTO posicion (latitud, longitud, idUsuario, idZona) VALUES (?,?,?,?)")
														.execute(Tuple.of(posicion.getLatitud(), posicion.getLongitud(), posicion.getIdUsuario(), idZona), res -> {
															if (res.succeeded()) {
																System.out.println("OK!");
																routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
																.end(gson.toJson(posicion));
																
															}else {
																System.out.println("Error: " + res.cause().getLocalizedMessage());
																routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
																.end();
															}}
													);

												//}

												//});
	
										}
									});
									
							
					
					
					
					
				} else {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
					.end();
				}
			});
			}catch (Exception e) {
				int na  = 0;
			}
	}
	
	private void getOneConcentracion(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("idPlaca"));
		int hours = Integer.parseInt(routingContext.request().getParam("hours"));
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				Long current_date = Calendar.getInstance().getTimeInMillis();
				current_date = current_date - hours * 60 * 60 * 1000;
				connection.result().preparedQuery("SELECT * FROM concentracion WHERE idPlaca = ? AND fecha > ?")
						.execute(Tuple.of(id, current_date), res -> { 
							if (res.succeeded()) {
								// Get the result set
								RowSet<Row> resultSet = res.result();
								List<Concentracion> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new Concentracion(elem.getInteger("idConcentracion"),
											elem.getDouble("concentracion"), elem.getLong("fecha"), elem.getInteger("idPlaca")));
								}
								System.out.println(result.toString());
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
								.end(gson.toJson(result));
							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
								.end();
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
				.end();
			}
		});
		
	}
	
	private void getOnePlaca(RoutingContext routingContext) {
		String modelo = (routingContext.request().getParam("modelo"));
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery("SELECT * FROM placa WHERE modelo = ?")
				.execute(Tuple.of(modelo), res -> {
							if (res.succeeded()) {
								// Get the result set
								RowSet<Row> resultSet = res.result();
								List<Placa> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new Placa(elem.getInteger("idPlaca"),
											elem.getString("modelo"), elem.getInteger("idZona")));
								}
								System.out.println(result.toString());
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
								.end(gson.toJson(result));
							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
								.end();
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
				.end();
			}
		});
		
	}
	
	
	private void addOneConcentracion(RoutingContext routingContext) {
		List<Double> result2 = new ArrayList<>();
		Double umbral = 69.1;
		try{
		final Concentracion concentracion = gson.fromJson(routingContext.getBodyAsString(), Concentracion.class);
		final Ayuda ayuda = gson.fromJson(routingContext.getBodyAsString(), Ayuda.class);
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery("INSERT INTO concentracion (concentracion, fecha, idPlaca) VALUES (?,?,?)")
						.execute(Tuple.of(concentracion.getConcentracion(), Calendar.getInstance().getTimeInMillis(), concentracion.getIdPlaca()),
						res -> {
							if (res.succeeded()) {
								System.out.println("OK!");
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
								.end(gson.toJson(concentracion));
						
								int idPlaca = concentracion.getIdPlaca();
								mySqlClient.preparedQuery("select idZona from placa where idPlaca = ?").execute(Tuple.of(idPlaca), new Handler<AsyncResult<RowSet<Row>>>() 
								{
									@Override
									public void handle(AsyncResult<RowSet<Row>> event) {
										int idZone = 0;
										for (Row row: event.result()) {
											idZone = row.getInteger(0);
										}
				
										mySqlClient.preparedQuery("REPLACE into ayuda(idZona, idPlaca, concentracion) value (?, ?, ?);").execute(Tuple.of(idZone,idPlaca,concentracion.getConcentracion()), res -> {
												if (res.succeeded()) {
													/*System.out.println("OK!");
													routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
													.end(gson.toJson(ayuda));
													*/
													}else {
														/*System.out.println("Error: " + res.cause().getLocalizedMessage());
														routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
														.end();*/
												}
											});
										
										mySqlClient.preparedQuery("select concentracion from ayuda where idZona = ?").execute(Tuple.of(idZone), new Handler<AsyncResult<RowSet<Row>>>() {

											@Override
											public void handle(AsyncResult<RowSet<Row>> event) {
												//List<Double> lista = new ArrayList<>();
												for (Row row: event.result()) {
													//System.out.println(row.getDouble("concentracion"))	;
													result2.add(row.getDouble("concentracion"));
												}
												System.out.println(result2)	;
												int i = 0;
												Boolean supera = false;
												for (Double resultado : result2) {
													if(resultado > umbral) {
														i++;
														if(i==result2.size()) {
															supera = true;
														}
													}
												}
												if(supera) {
													
													mqttClient.connect(1883, "localhost", s -> {
														mqttClient.publish("topic_1", Buffer.buffer("1"), MqttQoS.AT_LEAST_ONCE, false, false);
														});
													
												} else {
													mqttClient.connect(1883, "localhost", s -> {
														mqttClient.publish("topic_1", Buffer.buffer("0"), MqttQoS.AT_LEAST_ONCE, false, false);
														
														});

												}
											}
											
										});

											
									}
								});
								
								}else {
									System.out.println("Error: " + res.cause().getLocalizedMessage());
									routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
									.end();
							} 
						});
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
				.end();
			}
		});
		}catch (Exception e) {
			int na  = 0;
		}
	}
	
	
		
	private void getOnePosicion(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("idUsuario"));
		int latitud = Integer.parseInt(routingContext.request().getParam("latitud"));
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery("SELECT * FROM posicion WHERE idUsuario = ? AND latitud = ?")
						.execute(Tuple.of(id, latitud), res -> {
							if (res.succeeded()) {
								// Get the result set
								RowSet<Row> resultSet = res.result();
								List<Posicion> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new Posicion(elem.getInteger("idPosicion"),
											elem.getDouble("latitud"), elem.getDouble("longitud"), elem.getInteger("idUsuario"),elem.getInteger("idZona2")));
								}
								System.out.println(result.toString());
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
								.end(gson.toJson(result));
							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
								.end();
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
				.end();
			}
		});
		
	}
	/*
	private void getOneZona(RoutingContext routingContext) {
		int longitud = Integer.parseInt(routingContext.request().getParam("longitud"));
		int latitud = Integer.parseInt(routingContext.request().getParam("latitud"));
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery("SELECT * FROM zona WHERE longitud = ? AND latitud = ?")
						.execute(Tuple.of(longitud, latitud), res -> {
							if (res.succeeded()) {
								// Get the result set
								RowSet<Row> resultSet = res.result();
								List<Posicion> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new Posicion(elem.getInteger("idZona"),
											elem.getDouble("latitud"), elem.getDouble("longitud"), elem.getInteger("idUsuario")));
								}
								System.out.println(result.toString());
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
								.end(gson.toJson(result));
							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
								.end();
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
				.end();
			}
		});
		
	}
	*/
	private void addOnePlaca(RoutingContext routingContext) {
		try{
		final Placa placa = gson.fromJson(routingContext.getBodyAsString(), Placa.class);
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery("INSERT INTO placa (modelo, idZona) VALUES (?,?)")
						.execute(Tuple.of(placa.getModelo(), placa.getIdZona()),
						res -> {
							if (res.succeeded()) {
								System.out.println("OK!");
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
								.end(gson.toJson(placa));
								}else {
									System.out.println("Error: " + res.cause().getLocalizedMessage());
									routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
									.end();
							}
						});
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
				.end();
			}
		});
		}catch (Exception e) {
			int na  = 0;
		}
	}
	
/*
	@SuppressWarnings("unused")
	private void getAll(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(users.values()));
	}

	private void getAllWithParams(RoutingContext routingContext) {
		final String nombrePlaca = routingContext.queryParams().contains("nombrePlaca") ? 
				routingContext.queryParam("nombrePlaca").get(0) : null;
		
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(users.values().stream().filter(elem -> {
					boolean res = true;
					res = res && nombrePlaca != null ? elem.getNombrePlaca().equals(nombrePlaca) : true;
					return res;
				}).collect(Collectors.toList())));
	}

	private void getOne(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("userid"));
		if (users.containsKey(id)) {
			Covid ds = users.get(id);
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
					.end(gson.toJson(ds));
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}

	private void addOne(RoutingContext routingContext) {
		final Covid user = gson.fromJson(routingContext.getBodyAsString(), Covid.class);
		users.put(user.getModelo(), user);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(user));
	}

	private void deleteOne(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("userid"));
		if (users.containsKey(id)) {
			Covid user = users.get(id);
			users.remove(id);
			routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
					.end(gson.toJson(user));
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end();
		}
	}

	private void putOne(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("modelo"));
		Covid ds = users.get(id);
		final Covid element = gson.fromJson(routingContext.getBodyAsString(), Covid.class);
		
		ds.setFecha(element.getFecha());
		ds.setConcentracion(element.getConcentracion());
		ds.setModelo(element.getModelo());
		ds.setLatitud(element.getLatitud());
		ds.setLongitud(element.getLongitud());
		ds.setNombrePlaca(element.getNombrePlaca());
		
		users.put(ds.getModelo(), ds);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(element));
	}

	zona1: Sevilla
	zona2: Marmolejo
	zona3: NY


	private void createSomeData(int number) {
		Random rnd = new Random();
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			double concentracion = rnd.nextDouble();
			double latitud = rnd.nextDouble();
			double longitud = rnd.nextDouble();
			users.put(id, new Covid(id, concentracion + id, Calendar.getInstance().getTimeInMillis() + id,
					latitud + id, longitud +id, "nombrePlaca_" + id));
		});
	}
*/
}