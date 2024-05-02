
# SpringRest

ProfITSoft homework #2 - Spring Boot Application for CRUD operations with Movies.


## API Reference

#### Add new movie

```http
  POST /api/movie
```

| Parameter  | Type            | Description                |
| :--------  | :-------        | :------------------------- |
| `title`    | `string`        | **Required**. Movie title  |
| `year`     | `number`        | **Required**. Release year |
| `genre`    | `strings array` | **Required**. Genres       |
| `director` | `string`        | **Required**. Directed by  |

JSON example:
```
{
    "title": "Matrix",
    "year": 1999,
    "genre": ["Fantasy", "Action"],
    "director": "The Wachowskis"
}
```
___

#### Get movie by ID

```http
  GET /api/movie/{id}
```

| Path variable | Type     | Description                       |
| :--------     | :------- | :-------------------------------- |
| `id`          | `number` | **Required**. Id of item to fetch |

___

#### Edit movie by ID

```http
  PUT /api/movie/{id}
```

| Path variable | Type     | Description                       |
| :--------     | :------- | :-------------------------------- |
| `id`          | `number` | **Required**. Id of item to fetch |

| Parameter  | Type            | Description                |
| :--------  | :-------        | :------------------------- |
| `title`    | `string`        | Movie title  |
| `year`     | `number`        | Release year |
| `genre`    | `strings array` | Genres       |
| `director` | `string`        | Directed by  |

JSON example:
```
{
    "genre": ["Fantasy", "Action", "Drama"],
    "director": "Lana Wachowski, Lilly Wachowski"
}
```
___

#### Delete movie by ID

```http
  DELETE /api/movie/{id}
```

| Path variable | Type     | Description                       |
| :--------     | :------- | :-------------------------------- |
| `id`          | `number` | **Required**. Id of movie to fetch |

___

#### Find movies by query

```http
  POST /api/movie/_list
```

| Parameter  | Type            | Description                |
| :--------  | :-------        | :------------------------- |
| `title`    | `string`        | Movie title  |
| `year`     | `number`        | Release year |
| `genre`    | `strings array` | Genres       |
| `director` | `string`        | Directed by  |
| `page` | `number`        | **Required**. Result page number|
| `size` | `number`        | **Required**. Records on one page|

___

#### Find movies by query and get them in .csv file

```http
  POST /api/movie/_report
```

| Parameter  | Type            | Description                |
| :--------  | :-------        | :------------------------- |
| `title`    | `string`        | Movie title  |
| `year`     | `number`        | Release year |
| `genre`    | `strings array` | Genres       |
| `director` | `string`        | Directed by  |

___

#### Upload movies from JSON file

```http
  POST /api/movie/upload
```

| Parameter  | Type            | Description                   |
| :--------  | :-------        | :-------------------------    |
| `file`     | `File`        | **Required.** JSON file with movies to upload |

___

#### Get all directors

```http
  GET /api/director
```

#### Add new director

```http
  POST /api/director
```

| Parameter  | Type            | Description                |
| :--------  | :-------        | :------------------------- |
| `name`    | `string`        | **Required.** Full name of director|

#### Edit director by ID

```http
  PUT /api/director/{id}
```

| Path variable  | Type            | Description                |
| :--------  | :-------        | :------------------------- |
| `id`    | `number`        | **Required.** Id of director to fetch|

| Parameter  | Type            | Description                |
| :--------  | :-------        | :------------------------- |
| `name`    | `string`        | **Required.** New name of director|

#### Delete director by ID

```http
  DELETE /api/director/{id}
```

| Path variable  | Type            | Description                |
| :--------  | :-------        | :------------------------- |
| `id`    | `number`        | **Required.** Id of director to fetch|

___
## Run Locally

Clone the project

```bash
  git clone https://link-to-project
```

Go to the project directory

```bash
  cd my-project
```

Run 'SpringRestApplication'

