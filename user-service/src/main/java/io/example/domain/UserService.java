package io.example.domain;

import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.function.TransactionalDatabaseClient;
import org.springframework.data.r2dbc.function.convert.MappingR2dbcConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * The {@link UserService} contains methods for managing the transactional state of {@link User} entities. Each
 * method that alters the state of a {@link User} entity will allow you to specify a callback {@link Consumer<User>}.
 * The callback function provides you with a reference to the pre-committed {@link User} entity, allowing you to
 * perform a dual-write to a separate application before finalizing the transaction and committing the result to the
 * attached database.
 *
 * * {@Link UserService} chứa các phương pháp để quản lý trạng thái giao dịch của các thực thể {@link User}. Mỗi
 *   * phương thức thay đổi trạng thái của thực thể {@link User} sẽ cho phép bạn chỉ định {@link Consumer <User>} gọi lại.
 *   * Chức năng gọi lại cung cấp cho bạn tham chiếu đến thực thể {@link User} đã cam kết trước, cho phép bạn
 *   * thực hiện ghi kép vào một ứng dụng riêng biệt trước khi hoàn tất giao dịch và cam kết kết quả cho
 *   * cơ sở dữ liệu đính kèm.
 */

@Service
public class UserService {

	private final TransactionalDatabaseClient transactionalDatabaseClient;
	private final DatabaseClient databaseClient;
	private final MappingR2dbcConverter converter;

	public UserService(TransactionalDatabaseClient transactionalDatabaseClient, DatabaseClient databaseClient,
	                   MappingR2dbcConverter converter) {
		this.transactionalDatabaseClient = transactionalDatabaseClient;
		this.databaseClient = databaseClient;
		this.converter = converter;
	}

	/**
	 * Create a new {@link User} with a supplied callback {@link Consumer<User>} that will allow you to submit a domain
	 * event to a third-party system, such as Apache Kafka, before finalizing the commit.
	 *
	 * @param user     is the {@link User} entity to create.
	 * @param callback is a {@link Consumer<User>} that will allow you to throw an exception to rollback the TX.
	 * @return a {@link Mono<User>} that emits the result of the transaction in the form of the committed {@link User}.
	 */
	public Mono<User> create(User user, Consumer<User> callback) {
		return transactionalDatabaseClient.inTransaction(db -> db.insert().into(User.class)
				.using(user)
				.map((o, u) -> converter.populateIdIfNecessary(user).apply(o, u))
				.first()
				.map(User::getId)
				.flatMap(id -> db.execute().sql("SELECT * FROM users WHERE id=$1")
						.bind(0, id).as(User.class)
						.fetch()
						.first()).delayUntil(u -> Mono.fromRunnable(() -> callback.accept(u)))).single();
	}

	/**
	 * Uses a non-transactional database client to find a {@link User} by ID.
	 *
	 * @param id is the ID of the {@link User} that should be found.
	 * @return a {@link Mono<User>} that emits the result of the database lookup.
	 */
	public Mono<User> find(Long id) {
		return databaseClient.execute().sql("SELECT * FROM users WHERE id=$1")
				.bind(0, id).as(User.class)
				.fetch()
				.one()
				.single();
	}

	/**
	 * Updates an existing {@link User} entity by its unique ID.
	 *
	 * @param user     is {@link User} model that contains the updates to be applied to the existing entity.
	 * @param callback is a {@link Consumer<User>} that allows you to either commit or rollback the transaction.
	 * @return a {@link Mono<User>} that emits the result of the update transaction.
	 */
	public Mono<User> update(User user, Consumer<User> callback) {
		Assert.notNull(user.getId(), "User ID must not be null");

		AtomicReference<Long> userId = new AtomicReference<>();
		userId.set(user.getId());

		return transactionalDatabaseClient.inTransaction(db ->
				db.execute().sql("UPDATE users SET first_name=$1, last_name=$2 WHERE id=$3 RETURNING *")
						.bind(0, user.getFirstName())
						.bind(1, user.getLastName())
						.bind(2, user.getId()).as(User.class).fetch().rowsUpdated()
						.then(db.execute().sql("SELECT * FROM users WHERE id=$1")
								.bind(0, userId.get())
								.as(User.class)
								.fetch()
								.first()).delayUntil(u -> Mono.fromRunnable(() -> callback.accept(u)))).single();
	}
}
