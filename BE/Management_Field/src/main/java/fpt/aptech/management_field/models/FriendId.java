package fpt.aptech.management_field.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendId implements Serializable {
    private Long userId1;
    private Long userId2;
}