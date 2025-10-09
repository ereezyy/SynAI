"""Webhook handlers for external integrations."""

import hashlib
import hmac
from typing import Any, Dict

from fastapi import APIRouter, Header, HTTPException, Request, status
from loguru import logger
from pydantic import BaseModel

router = APIRouter()


class WebhookEvent(BaseModel):
    """Webhook event model."""

    event_type: str
    source: str
    data: Dict[str, Any]
    timestamp: str


class WebhookResponse(BaseModel):
    """Webhook response model."""

    received: bool
    processed: bool
    message: str


def verify_webhook_signature(
    payload: bytes,
    signature: str,
    secret: str
) -> bool:
    """
    Verify webhook signature.

    Args:
        payload: Request payload
        signature: Provided signature
        secret: Webhook secret

    Returns:
        True if signature is valid
    """
    expected_signature = hmac.new(
        secret.encode(),
        payload,
        hashlib.sha256
    ).hexdigest()

    return hmac.compare_digest(signature, expected_signature)


@router.post("/github", response_model=WebhookResponse)
async def github_webhook(
    request: Request,
    x_hub_signature_256: str = Header(None)
) -> WebhookResponse:
    """
    Handle GitHub webhook events.

    Args:
        request: FastAPI request
        x_hub_signature_256: GitHub signature header

    Returns:
        Webhook response
    """
    try:
        payload = await request.body()

        if x_hub_signature_256:
            secret = "your-webhook-secret"
            if not verify_webhook_signature(
                payload,
                x_hub_signature_256.replace("sha256=", ""),
                secret
            ):
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Invalid signature"
                )

        data = await request.json()

        logger.info(f"Received GitHub webhook: {data.get('action', 'unknown')}")

        return WebhookResponse(
            received=True,
            processed=True,
            message="Webhook processed successfully"
        )

    except Exception as e:
        logger.error(f"Error processing GitHub webhook: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


@router.post("/slack", response_model=WebhookResponse)
async def slack_webhook(
    request: Request
) -> WebhookResponse:
    """
    Handle Slack webhook events.

    Args:
        request: FastAPI request

    Returns:
        Webhook response
    """
    try:
        data = await request.json()

        if data.get("type") == "url_verification":
            return {"challenge": data.get("challenge")}

        logger.info(f"Received Slack webhook: {data.get('event', {}).get('type', 'unknown')}")

        return WebhookResponse(
            received=True,
            processed=True,
            message="Webhook processed successfully"
        )

    except Exception as e:
        logger.error(f"Error processing Slack webhook: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


@router.post("/custom", response_model=WebhookResponse)
async def custom_webhook(
    event: WebhookEvent
) -> WebhookResponse:
    """
    Handle custom webhook events.

    Args:
        event: Webhook event

    Returns:
        Webhook response
    """
    try:
        logger.info(f"Received custom webhook: {event.event_type} from {event.source}")

        return WebhookResponse(
            received=True,
            processed=True,
            message=f"Processed {event.event_type} event"
        )

    except Exception as e:
        logger.error(f"Error processing custom webhook: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )
