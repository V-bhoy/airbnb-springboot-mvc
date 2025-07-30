package com.vb.projects.airBnbApp.repository;

import com.vb.projects.airBnbApp.entity.Hotel;
import com.vb.projects.airBnbApp.entity.Inventory;
import com.vb.projects.airBnbApp.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    void deleteByDateAfterAndRoom(LocalDate date, Room room);

    @Query("""
SELECT distinct i.hotel from Inventory i
   where i.city = :city
   and i.date between :startDate and :endDate
   and i.closed = false
   and (i.totalCount - i.bookedCount - i.reservedCount >= :roomsCount)
   group by i.hotel, i.room
   having count(i.date) = :dateCount
""")
    Page<Hotel> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );



    @Query("""
            SELECT i 
            from Inventory i
            where i.room.id = :roomId
            and i.date between :startDate and :endDate
            and (i.totalCount - i.bookedCount - i.reservedCount >= :roomsCount)
           """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockAvailableInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount
    );

    @Query("""
            SELECT i 
            from Inventory i
            where i.room.id = :roomId
            and i.date between :startDate and :endDate
            and i.closed = false
            and (i.totalCount - i.bookedCount >= :numberOfRooms)
           """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockReservedInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("numberOfRooms") int numberOfRooms
    );

    @Modifying
    @Query("""
            UPDATE Inventory i 
            set i.reservedCount = i.reservedCount + :numberOfRooms
            where i.room.id = :roomId
            and i.date between :startDate and :endDate
            and (i.totalCount - i.bookedCount - i.reservedCount)  >= :numberOfRooms
            and i.closed = false
           """)
    void initBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("numberOfRooms") int numberOfRooms
    );


    @Modifying
    @Query("""
            UPDATE Inventory i 
            set i.reservedCount = i.reservedCount - :numberOfRooms,
                i.bookedCount = i.bookedCount + :numberOfRooms
            where i.room.id = :roomId
            and i.date between :startDate and :endDate
            and (i.totalCount - i.bookedCount)  >= :numberOfRooms
            and i.closed = false
           """)
    void confirmBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("numberOfRooms") int numberOfRooms
    );

    @Modifying
    @Query("""
            UPDATE Inventory i 
            set i.bookedCount = i.bookedCount - :numberOfRooms
            where i.room.id = :roomId
            and i.date between :startDate and :endDate
            and (i.totalCount - i.bookedCount)  >= :numberOfRooms
            and i.reservedCount >= :numberOfRooms
            and i.closed = false
           """)
    void cancelBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("numberOfRooms") int numberOfRooms
    );

    List<Inventory> findByHotelAndDateBetween(Hotel hotel, LocalDate startDate, LocalDate endDate);
}
