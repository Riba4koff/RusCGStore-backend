package ru.cgstore.storage.users

import arrow.core.Either
import ru.cgstore.models.Failure
import ru.cgstore.models.users.User
import ru.cgstore.models.users.UserRole
import ru.cgstore.requests.profile.UpdateProfileRequest
import ru.cgstore.requests.users.SignUpUserRequest
import ru.cgstore.responses.UserDTO
import ru.cgstore.responses.profile.ProfileDataResponse
import ru.cgstore.security.hash_service.SaltedHash

interface UsersService {
    suspend fun create(request: SignUpUserRequest, saltedHash: SaltedHash): Either<Failure, Unit>
    suspend fun read(id: String): Either<Failure, ProfileDataResponse>
    suspend fun read(): Either<Failure, List<UserDTO>>
    suspend fun readByLogin(login: String): Either<Failure, User>
    suspend fun loginExists(login: String): Either<Failure, Unit>
    suspend fun emailExists(email: String): Either<Failure, Unit>
    suspend fun phoneExists(phone: String): Either<Failure, Unit>
    suspend fun update(user_id: String, request: UpdateProfileRequest): Either<Failure, Unit>
    suspend fun setRole(user_id: String, role: UserRole): Either<Failure, Unit>
    suspend fun block(id: String): Either<Failure, Unit>
    suspend fun unblock(id: String): Either<Failure, Unit>
}