package com.fzm.mall.constant.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseEnum {
    success(0),
    failure(1),
    invalid_parameter(1),
    invalid_parameter_with_args(1),
    too_many_requests_plz_try_again_later(2),

    not_logged_in(301),
    login_expired(302),
    account_frozen(303),
    permission_denied(401),
    not_found(404),

    invalid_signature_content(1),
    signature_verification_failed(1),
    signature_expired(1),
    invalid_signature_timestamp(1),

    invalid_address_format(1),
    cannot_add_this_address(1),

    invalid_referral_address(1),
    referral_address_cannot_be_the_same_as_your_current_address(1),
    referral_address_is_not_registered(1),
    circular_reference_detected_in_referral_addresses(1),

    file_error(1),
    invalid_file_name(1),
    file_not_upload(1),
    file_upload_failed(1),
    file_download_failed(1),

    Invalid_transaction_hash(1),
    transaction_hash_not_confirmed(1),
    transaction_hash_already_used(1),
    invalid_payment_amount(1),
    insufficient_payment_amount(1),

    insufficient_gas(1),
    insufficient_balance(1),


    invalid_goods_id(1),
    insufficient_goods_stock(1),


    order_quantity_exceeds_purchase_limit(1),
    Maximum_num_per_order(1),

    maximum_limit_already_reached(1),
    ;
    private final int code;
}
