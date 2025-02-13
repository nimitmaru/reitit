# OpenAPI Support

**Stability: alpha**

Reitit can generate [OpenAPI 3.1.0](https://spec.openapis.org/oas/v3.1.0)
documentation. The feature works similarly to [Swagger documentation](swagger.md).

The [http-swagger](../../examples/http-swagger) and
[ring-malli-swagger](../../examples/ring-malli-swagger) examples also
have OpenAPI documentation.

## OpenAPI data

The following route data keys contribute to the generated swagger specification:

| key            | description |
| ---------------|-------------|
| :openapi       | map of any openapi data. Can contain keys like `:deprecated`.
| :content-types | vector of supported content types. Defaults to `["application/json"]`
| :no-doc        | optional boolean to exclude endpoint from api docs
| :tags          | optional set of string or keyword tags for an endpoint api docs
| :summary       | optional short string summary of an endpoint
| :description   | optional long description of an endpoint. Supports http://spec.commonmark.org/

Coercion keys also contribute to the docs:

| key           | description |
| --------------|-------------|
| :parameters   | optional input parameters for a route, in a format defined by the coercion
| :responses    | optional descriptions of responses, in a format defined by coercion

Use `:request` parameter coercion (instead of `:body`) to unlock per-content-type coercions. See [Coercion](coercion.md).

## Custom OpenAPI data

The `:openapi` route data key can be used to add top-level or
route-level information to the generated OpenAPI spec. This is useful
for providing `"securitySchemes"`, `"examples"` or other OpenAPI keys
that are not generated automatically by reitit.

```clj
["/foo"
 {:post {:parameters {:body {:name string? :age int?}}
         :openapi {:requestBody
                   {:content
                    {"application/json"
                     {:examples {"Pyry" {:summary "Pyry, 45y"
                                         :value {:name "Pyry" :age 45}}
                                 "Cat" {:summary "Cat, 8y"
                                        :value {:name "Cat" :age 8}}}}}}}
         ...}}]
```

See [the http-swagger example](../../examples/http-swagger) for a
working examples of `"securitySchemes"` and `"examples"`.

## OpenAPI spec

Serving the OpenAPI specification is handled by `reitit.openapi/create-openapi-handler`. It takes no arguments and returns a ring handler which collects at request-time data from all routes and returns an OpenAPI specification as Clojure data, to be encoded by a response formatter.

You can use the `:openapi` route data key of the `create-openapi-handler` route to populate the top level of the OpenAPI spec.

Example:

```
["/openapi.json"
 {:get {:handler (openapi/create-openapi-handler)
        :openapi {:info {:title "my nice api" :version "0.0.1"}}
        :no-doc true}}]
```

If you need to post-process the generated spec, just wrap the handler with a custom `Middleware` or an `Interceptor`.

## Swagger-ui

[Swagger-UI](https://github.com/swagger-api/swagger-ui) is a user interface to visualize and interact with the Swagger specification. To make things easy, there is a pre-integrated version of the swagger-ui as a separate module.

Note: you need Swagger-UI 5 for OpenAPI 3.1 support. As of 2023-03-10, a v5.0.0-alpha.0 is out.
