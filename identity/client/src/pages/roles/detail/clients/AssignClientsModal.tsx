/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import { FC, useEffect, useState } from "react";
import useTranslate from "src/utility/localization";
import { useApiCall } from "src/utility/api/hooks";
import { Client } from "src/utility/api/roles";
import FormModal from "src/components/modal/FormModal";
import { assignRoleClient, Role } from "src/utility/api/roles";
import TextField from "src/components/form/TextField";
import { UseEntityModalProps } from "src/components/modal";

const AssignClientsModal: FC<UseEntityModalProps<Role["roleId"]>> = ({
  entity: roleId,
  onSuccess,
  open,
  onClose,
}) => {
  const { t, Translate } = useTranslate("roles");
  const [clientId, setClientId] = useState<Client["clientId"]>("");
  const [loadingAssignClient, setLoadingAssignClient] = useState(false);

  const [callAssignClient] = useApiCall(assignRoleClient);

  const canSubmit = roleId && clientId.length;

  const handleSubmit = async () => {
    if (!canSubmit) return;

    setLoadingAssignClient(true);
    const { success } = await callAssignClient({
      clientId,
      roleId,
    });
    setLoadingAssignClient(false);

    if (success) {
      onSuccess();
    }
  };

  useEffect(() => {
    if (open) {
      setClientId("");
    }
  }, [open]);

  return (
    <FormModal
      headline={t("assignClient")}
      confirmLabel={t("assignClient")}
      loading={loadingAssignClient}
      loadingDescription={t("assigningClient")}
      open={open}
      onSubmit={handleSubmit}
      submitDisabled={!canSubmit}
      onClose={onClose}
      overflowVisible
    >
      <p>
        <Translate i18nKey="assignClientToRole">
          Assign client to this role
        </Translate>
      </p>

      <TextField
        label={t("clientId")}
        placeholder={t("enterClientId")}
        onChange={setClientId}
        value={clientId}
        autoFocus
      />
    </FormModal>
  );
};

export default AssignClientsModal;
